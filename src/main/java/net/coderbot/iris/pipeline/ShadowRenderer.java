package net.coderbot.iris.pipeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import net.coderbot.iris.mixin.RenderGlobalAccessor;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.*;

import com.google.common.collect.ImmutableList;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.shaderpack.OptionalBoolean;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.coderbot.iris.shadows.CullingDataCache;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.CullEverythingFrustum;
import net.coderbot.iris.shadows.frustum.FrustumHolder;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.coderbot.iris.uniforms.CameraUniforms;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CelestialUniforms;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;

public class ShadowRenderer {
	public static Matrix4f MODELVIEW;
	public static Matrix4f PROJECTION;
	public static List<TileEntity> visibleBlockEntities;
	public static boolean ACTIVE = false;
	private final float halfPlaneLength;
	private final float renderDistanceMultiplier;
	private final float entityShadowDistanceMultiplier;
	private final int resolution;
	private final float intervalSize;
	private final Float fov;
	private final ShadowRenderTargets targets;
	private final OptionalBoolean packCullingState;
	private boolean packHasVoxelization;
	private final boolean shouldRenderTerrain;
	private final boolean shouldRenderTranslucent;
	private final boolean shouldRenderEntities;
	private final boolean shouldRenderPlayer;
	private final boolean shouldRenderBlockEntities;
	private final float sunPathRotation;
	private final List<MipmapPass> mipmapPasses = new ArrayList<>();
	private final String debugStringOverall;
	private FrustumHolder terrainFrustumHolder;
	private FrustumHolder entityFrustumHolder;
	private String debugStringTerrain = "(unavailable)";
	private int renderedShadowEntities = 0;
	private int renderedShadowBlockEntities = 0;
	private Profiler profiler;

	public ShadowRenderer(ProgramSource shadow, PackDirectives directives,
						  ShadowRenderTargets shadowRenderTargets) {

		this.profiler = Minecraft.getMinecraft().profiler;

		final PackShadowDirectives shadowDirectives = directives.getShadowDirectives();

		this.halfPlaneLength = shadowDirectives.getDistance();
		this.renderDistanceMultiplier = shadowDirectives.getDistanceRenderMul();
		this.entityShadowDistanceMultiplier = shadowDirectives.getEntityShadowDistanceMul();
		this.resolution = shadowDirectives.getResolution();
		this.intervalSize = shadowDirectives.getIntervalSize();
		this.shouldRenderTerrain = shadowDirectives.shouldRenderTerrain();
		this.shouldRenderTranslucent = shadowDirectives.shouldRenderTranslucent();
		this.shouldRenderEntities = shadowDirectives.shouldRenderEntities();
		this.shouldRenderPlayer = shadowDirectives.shouldRenderPlayer();
		this.shouldRenderBlockEntities = shadowDirectives.shouldRenderBlockEntities();

		debugStringOverall = "half plane = " + halfPlaneLength + " meters @ " + resolution + "x" + resolution;

		this.terrainFrustumHolder = new FrustumHolder();
		this.entityFrustumHolder = new FrustumHolder();

		this.fov = shadowDirectives.getFov();
		this.targets = shadowRenderTargets;

		if (shadow != null) {
			// Assume that the shader pack is doing voxelization if a geometry shader is detected.
			// Also assume voxelization if image load / store is detected.
			this.packHasVoxelization = shadow.getGeometrySource().isPresent();
			this.packCullingState = shadowDirectives.getCullingState();
		} else {
			this.packHasVoxelization = false;
			this.packCullingState = OptionalBoolean.DEFAULT;
		}

		this.sunPathRotation = directives.getSunPathRotation();

		configureSamplingSettings(shadowDirectives);
	}

	public void setUsesImages(boolean usesImages) {
		this.packHasVoxelization = packHasVoxelization || usesImages;
	}

	public static void createShadowModelView(float sunPathRotation, float intervalSize) {
		// Determine the camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x;
		double cameraY = cameraPos.y;
		double cameraZ = cameraPos.z;

		// Set up our modelview matrix stack
		ShadowMatrices.createModelViewMatrix(getShadowAngle(), intervalSize, sunPathRotation, cameraX, cameraY, cameraZ);
	}

	private static WorldClient getLevel() {
		return Objects.requireNonNull(Minecraft.getMinecraft().world);
	}

	private static float getSkyAngle() {
		return getLevel().getCelestialAngle(CapturedRenderingState.INSTANCE.getTickDelta());
	}

	private static float getSunAngle() {
		float skyAngle = getSkyAngle();

		if (skyAngle < 0.75F) {
			return skyAngle + 0.25F;
		} else {
			return skyAngle - 0.75F;
		}
	}

	private static float getShadowAngle() {
		float shadowAngle = getSunAngle();

		if (!CelestialUniforms.isDay()) {
			shadowAngle -= 0.5F;
		}

		return shadowAngle;
	}

	private void configureSamplingSettings(PackShadowDirectives shadowDirectives) {
		final ImmutableList<PackShadowDirectives.DepthSamplingSettings> depthSamplingSettings =
				shadowDirectives.getDepthSamplingSettings();

		final ImmutableList<PackShadowDirectives.SamplingSettings> colorSamplingSettings =
				shadowDirectives.getColorSamplingSettings();

		GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);

		configureDepthSampler(targets.getDepthTexture().getTextureId(), depthSamplingSettings.get(0));

		configureDepthSampler(targets.getDepthTextureNoTranslucents().getTextureId(), depthSamplingSettings.get(1));

		for (int i = 0; i < colorSamplingSettings.size(); i++) {
			int glTextureId = targets.get(i).getMainTexture();

			configureSampler(glTextureId, colorSamplingSettings.get(i));
		}

		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
	}

	private void configureDepthSampler(int glTextureId, PackShadowDirectives.DepthSamplingSettings settings) {
		if (settings.getHardwareFiltering()) {
			// We have to do this or else shadow hardware filtering breaks entirely!
			IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL30.GL_COMPARE_REF_TO_TEXTURE);
		}

		// Workaround for issues with old shader packs like Chocapic v4.
		// They expected the driver to put the depth value in z, but it's supposed to only
		// be available in r. So we set up the swizzle to fix that.
		IrisRenderSystem.texParameteriv(glTextureId, GL11.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
				new int[] { GL11.GL_RED, GL11.GL_RED, GL11.GL_RED, GL11.GL_ONE });

		configureSampler(glTextureId, settings);
	}

	private void configureSampler(int glTextureId, PackShadowDirectives.SamplingSettings settings) {
		if (settings.getMipmap()) {
			int filteringMode = settings.getNearest() ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR;
			mipmapPasses.add(new MipmapPass(glTextureId, filteringMode));
		}

		if (!settings.getNearest()) {
			// Make sure that things are smoothed
			IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		} else {
			IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			IrisRenderSystem.texParameteri(glTextureId, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
	}

	private void generateMipmaps() {
		GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);

		for (MipmapPass mipmapPass : mipmapPasses) {
			setupMipmappingForTexture(mipmapPass.getTexture(), mipmapPass.getTargetFilteringMode());
		}

		GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
	}

	private void setupMipmappingForTexture(int texture, int filteringMode) {
		IrisRenderSystem.generateMipmaps(texture, GL11.GL_TEXTURE_2D);
		IrisRenderSystem.texParameteri(texture, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filteringMode);
	}

	private FrustumHolder createShadowFrustum(float renderMultiplier, FrustumHolder holder) {
		// TODO: Cull entities / block entities with Advanced Frustum Culling even if voxelization is detected.
		String distanceInfo;
		String cullingInfo;
		if ((packCullingState == OptionalBoolean.FALSE || packHasVoxelization) && packCullingState != OptionalBoolean.TRUE) {
			double distance = halfPlaneLength * renderMultiplier;

			String reason;

			if (packCullingState == OptionalBoolean.FALSE) {
				reason = "(set by shader pack)";
			} else /*if (packHasVoxelization)*/ {
				reason = "(voxelization detected)";
			}

			if (distance <= 0 || distance > Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16) {
				distanceInfo = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16
						+ " blocks (capped by normal render distance)";
				cullingInfo = "disabled " + reason;
				return holder.setInfo(new NonCullingFrustum(), distanceInfo, cullingInfo);
			} else {
				distanceInfo = distance + " blocks (set by shader pack)";
				cullingInfo = "distance only " + reason;
				BoxCuller boxCuller = new BoxCuller(distance);
				holder.setInfo(new BoxCullingFrustum(boxCuller), distanceInfo, cullingInfo);
			}
		} else {
			BoxCuller boxCuller;

			double distance = halfPlaneLength * renderMultiplier;
			String setter = "(set by shader pack)";

			if (renderMultiplier < 0) {
				distance = IrisVideoSettings.shadowDistance * 16;
				setter = "(set by user)";
			}

			if (distance >= Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16) {
				distanceInfo = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16
						+ " blocks (capped by normal render distance)";
				boxCuller = null;
			} else {
				distanceInfo = distance + " blocks " + setter;

				if (distance == 0.0) {
					cullingInfo = "no shadows rendered";
					holder.setInfo(new CullEverythingFrustum(), distanceInfo, cullingInfo);
				}

				boxCuller = new BoxCuller(distance);
			}

			cullingInfo = "Advanced Frustum Culling enabled";

			Vector4f shadowLightPosition = new CelestialUniforms(sunPathRotation).getShadowLightPositionInWorldSpace();

			net.coderbot.iris.vendored.joml.Vector3f shadowLightVectorFromOrigin =
					new net.coderbot.iris.vendored.joml.Vector3f(shadowLightPosition.x(), shadowLightPosition.y(), shadowLightPosition.z());

			shadowLightVectorFromOrigin.normalize();

			return holder.setInfo(new AdvancedShadowCullingFrustum( new net.coderbot.iris.vendored.joml.Matrix4f(
					CapturedRenderingState.INSTANCE.getGbufferProjection().m00, CapturedRenderingState.INSTANCE.getGbufferProjection().m01,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m02, CapturedRenderingState.INSTANCE.getGbufferProjection().m03,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m10, CapturedRenderingState.INSTANCE.getGbufferProjection().m11,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m12, CapturedRenderingState.INSTANCE.getGbufferProjection().m13,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m20, CapturedRenderingState.INSTANCE.getGbufferProjection().m21,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m22, CapturedRenderingState.INSTANCE.getGbufferProjection().m23,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m30, CapturedRenderingState.INSTANCE.getGbufferProjection().m31,
					CapturedRenderingState.INSTANCE.getGbufferProjection().m32, CapturedRenderingState.INSTANCE.getGbufferProjection().m33
			),
					new net.coderbot.iris.vendored.joml.Matrix4f(
							CapturedRenderingState.INSTANCE.getGbufferProjection().m00, CapturedRenderingState.INSTANCE.getGbufferProjection().m01,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m02, CapturedRenderingState.INSTANCE.getGbufferProjection().m03,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m10, CapturedRenderingState.INSTANCE.getGbufferProjection().m11,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m12, CapturedRenderingState.INSTANCE.getGbufferProjection().m13,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m20, CapturedRenderingState.INSTANCE.getGbufferProjection().m21,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m22, CapturedRenderingState.INSTANCE.getGbufferProjection().m23,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m30, CapturedRenderingState.INSTANCE.getGbufferProjection().m31,
							CapturedRenderingState.INSTANCE.getGbufferProjection().m32, CapturedRenderingState.INSTANCE.getGbufferProjection().m33
					), shadowLightVectorFromOrigin, boxCuller), distanceInfo, cullingInfo);
		}

		return holder;
	}

	private void setupGlState(float[] projMatrix) {
		// Set up our projection matrix and load it into the legacy matrix stack
		IrisRenderSystem.setupProjectionMatrix(projMatrix);

		// Disable backface culling
		// This partially works around an issue where if the front face of a mountain isn't visible, it casts no
		// shadow.
		//
		// However, it only partially resolves issues of light leaking into caves.
		//
		// TODO: Better way of preventing light from leaking into places where it shouldn't
		GlStateManager.disableCull();
	}

	private void restoreGlState() {
		// Restore backface culling
		GlStateManager.enableCull();

		// Make sure to unload the projection matrix
		IrisRenderSystem.restoreProjectionMatrix();
	}

	private void copyPreTranslucentDepth() {
		profiler.endStartSection("translucent depth copy");

		targets.copyPreTranslucentDepth();
	}

	private void renderEntities(RenderGlobalAccessor worldRenderer, Frustum frustum, double cameraX, double cameraY, double cameraZ, float yaw, float tickDelta, boolean hasEntityFrustum) {
		RenderManager dispatcher = worldRenderer.getRenderManager();

		int shadowEntities = 0;

		profiler.startSection("cull");

		List<Entity> renderedEntities = new ArrayList<>(32);

		// TODO: I'm sure that this can be improved / optimized.
		for (Entity entity : getLevel().loadedEntityList) {
			if (!dispatcher.shouldRender(entity, frustum, cameraX, cameraY, cameraZ) || (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator())) {
				continue;
			}

			renderedEntities.add(entity);
		}

		profiler.endStartSection("sort");

		// Sort the entities by type first in order to allow vanilla's entity batching system to work better.
		// TODO not sure
		renderedEntities.sort(Comparator.comparingInt(entity -> entity.getClass().hashCode()));

		profiler.endStartSection("build geometry");

		for (Entity entity : renderedEntities) {
			// todo not sure yaw
			worldRenderer.getRenderManager().renderEntity(entity, cameraX, cameraY, cameraZ, yaw, tickDelta, hasEntityFrustum);
			shadowEntities++;
		}

		renderedShadowEntities = shadowEntities;

		profiler.endSection();
	}

	private void renderPlayerEntity(RenderGlobalAccessor worldRenderer, Frustum frustum, double cameraX, double cameraY, double cameraZ, float yaw, float tickDelta, boolean hasEntityFrustum) {
		RenderManager dispatcher = worldRenderer.getRenderManager();

		profiler.startSection("cull");

		EntityPlayer player = Minecraft.getMinecraft().player;

		if (!dispatcher.shouldRender(player, frustum, cameraX, cameraY, cameraZ) || player.isSpectator()) {
			return;
		}

		profiler.endStartSection("build geometry");

		int shadowEntities = 0;

		if (!player.getPassengers().isEmpty()) {
			for (int i = 0; i < player.getPassengers().size(); i++) {
				// todo : not sure if yaw is correct
				worldRenderer.getRenderManager().renderEntity(player.getPassengers().get(i), cameraX, cameraY, cameraZ, yaw, tickDelta, hasEntityFrustum);
				shadowEntities++;
			}
		}

		if (player.getRidingEntity() != null) {
			worldRenderer.getRenderManager().renderEntity(player.getRidingEntity(), cameraX, cameraY, cameraZ, yaw, tickDelta, hasEntityFrustum);
			shadowEntities++;
		}

		worldRenderer.getRenderManager().renderEntity(player, cameraX, cameraY, cameraZ, yaw, tickDelta, hasEntityFrustum);

		shadowEntities++;

		renderedShadowEntities = shadowEntities;

		profiler.endSection();
	}

	private void renderBlockEntities(double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum) {
		profiler.startSection("build blockentities");

		int shadowBlockEntities = 0;
		BoxCuller culler = null;
		if (hasEntityFrustum) {
			culler = new BoxCuller(halfPlaneLength * (renderDistanceMultiplier * entityShadowDistanceMultiplier));
			culler.setPosition(cameraX, cameraY, cameraZ);
		}

		for (TileEntity entity : visibleBlockEntities) {
			BlockPos pos = entity.getPos();
			if (hasEntityFrustum) {
				if (culler.isCulled(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
					continue;
				}
			}
			//modelView.pushPose();
			//modelView.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
			TileEntityRendererDispatcher.instance.render(entity, pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ, tickDelta);
			//modelView.popPose();

			shadowBlockEntities++;
		}

		renderedShadowBlockEntities = shadowBlockEntities;

		profiler.endSection();
	}

	public void renderShadows(RenderGlobalAccessor worldRenderer, ICamera playerCamera) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			return;
		}

		// We have to re-query this each frame since this changes based on whether the profiler is active
		// If the profiler is inactive, it will return InactiveProfiler.INSTANCE
		this.profiler = Minecraft.getMinecraft().profiler;

		Minecraft client = Minecraft.getMinecraft();

		profiler.startSection("shadows");
		ACTIVE = true;

		// NB: We store the previous player buffers in order to be able to allow mods rendering entities in the shadow pass (Flywheel) to use the shadow buffers instead.
		// RenderBuffers playerBuffers = worldRenderer.getRenderBuffers();
		// worldRenderer.setRenderBuffers(buffers);

		visibleBlockEntities = new ArrayList<>();

		// Create our camera
		// todo
		// PoseStack modelView = createShadowModelView(this.sunPathRotation, this.intervalSize);
		// MODELVIEW = modelView.last().pose().copy();
		float[] projMatrix;
		if (this.fov != null) {
			// If FOV is not null, the pack wants a perspective based projection matrix. (This is to support legacy packs)
			projMatrix = ShadowMatrices.createPerspectiveMatrix(this.fov);
		} else {
			projMatrix = ShadowMatrices.createOrthoMatrix(halfPlaneLength);
		}

		PROJECTION = new Matrix4f(projMatrix);

		profiler.endStartSection("terrain_setup");

		if (worldRenderer instanceof CullingDataCache) {
			((CullingDataCache) worldRenderer).saveState();
		}

		profiler.endStartSection("initialize frustum");

		terrainFrustumHolder = createShadowFrustum(renderDistanceMultiplier, terrainFrustumHolder);

		// Determine the player camera position
		Vector3d cameraPos = CameraUniforms.getUnshiftedCameraPosition();

		double cameraX = cameraPos.x();
		double cameraY = cameraPos.y();
		double cameraZ = cameraPos.z();
		// todo not sure
		float yaw = client.player.rotationYaw;

		// Center the frustum on the player camera position
		terrainFrustumHolder.getFrustum().setPosition(cameraX, cameraY, cameraZ);

		profiler.endSection();

		// Disable chunk occlusion culling - it's a bit complex to get this properly working with shadow rendering
		// as-is, however in the future it will be good to work on restoring it for a nice performance boost.
		//
		// TODO: Get chunk occlusion working with shadows
		boolean wasChunkCullingEnabled = client.renderChunksMany;
		client.renderChunksMany = false;

		// Always schedule a terrain update
		// TODO: Only schedule a terrain update if the sun / moon is moving, or the shadow map camera moved.
		// We have to ensure that we don't regenerate clouds every frame, since that's what needsUpdate ends up doing.
		// This took up to 10% of the frame time before we applied this fix! That's really bad!
//		int regenerateClouds = Minecraft.getMinecraft().gameSettings.shouldRenderClouds();
		((RenderGlobal) worldRenderer).setDisplayListEntitiesDirty();
//		Minecraft.getMinecraft().gameSettings.clouds = regenerateClouds;

		// Execute the vanilla terrain setup / culling routines using our shadow frustum.
//		worldRenderer.invokeSetupTerrain(playerCamera, terrainFrustumHolder.getFrustum(), false, worldRenderer.getFrameId(), Minecraft.getMinecraft().debugRenderer.shouldRender());
		// todo not sure
		worldRenderer.invokeSetupTerrain(client.getRenderViewEntity(), client.getRenderPartialTicks(), playerCamera, client.getFrameTimer().getIndex(), Minecraft.getMinecraft().debugRenderer.shouldRender());

		// Don't forget to increment the frame counter! This variable is arbitrary and only used in terrain setup,
		// and if it's not incremented, the vanilla culling code will get confused and think that it's already seen
		// chunks during traversal, and break rendering in concerning ways.
		// todo: not sure
//		worldRenderer.setCloudTickCounter(worldRenderer.getCloudTickCounter() + 1);

		client.renderChunksMany = wasChunkCullingEnabled;

		profiler.endStartSection("terrain");

		setupGlState(projMatrix);

		// Render all opaque terrain unless pack requests not to
		if (shouldRenderTerrain) {
			worldRenderer.invokeRenderBlockLayer(BlockRenderLayer.SOLID);
			worldRenderer.invokeRenderBlockLayer(BlockRenderLayer.CUTOUT);
			worldRenderer.invokeRenderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
		}

		profiler.endStartSection("entities");

		// Get the current tick delta. Normally this is the same as client.getTickDelta(), but when the game is paused,
		// it is set to a fixed value.
		final float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();

		// Create a constrained shadow frustum for entities to avoid rendering faraway entities in the shadow pass,
		// if the shader pack has requested it. Otherwise, use the same frustum as for terrain.
		boolean hasEntityFrustum = false;

		if (entityShadowDistanceMultiplier == 1.0F || entityShadowDistanceMultiplier < 0.0F) {
			entityFrustumHolder.setInfo(terrainFrustumHolder.getFrustum(), terrainFrustumHolder.getDistanceInfo(), terrainFrustumHolder.getCullingInfo());
		} else {
			hasEntityFrustum = true;
			entityFrustumHolder = createShadowFrustum(renderDistanceMultiplier * entityShadowDistanceMultiplier, entityFrustumHolder);
		}

		Frustum entityShadowFrustum = entityFrustumHolder.getFrustum();
		entityShadowFrustum.setPosition(cameraX, cameraY, cameraZ);

		// Render nearby entities
		//
		// Note: We must use a separate BuilderBufferStorage object here, or else very weird things will happen during
		// rendering.
		if (shouldRenderEntities) {
			renderEntities(worldRenderer, entityShadowFrustum, cameraX, cameraY, cameraZ, yaw, tickDelta, hasEntityFrustum);
		} else if (shouldRenderPlayer) {
			renderPlayerEntity(worldRenderer, entityShadowFrustum, cameraX, cameraY, cameraZ, yaw, tickDelta, hasEntityFrustum);
		}

		if (shouldRenderBlockEntities) {
			renderBlockEntities(cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum);
		}

		profiler.endStartSection("draw entities");

		// NB: Don't try to draw the translucent parts of entities afterwards. It'll cause problems since some
		// shader packs assume that everything drawn afterwards is actually translucent and should cast a colored
		// shadow...

		copyPreTranslucentDepth();

		profiler.endStartSection("translucent terrain");

		// TODO: Prevent these calls from scheduling translucent sorting...
		// It doesn't matter a ton, since this just means that they won't be sorted in the normal rendering pass.
		// Just something to watch out for, however...
		if (shouldRenderTranslucent) {
			worldRenderer.invokeRenderBlockLayer(BlockRenderLayer.TRANSLUCENT);
		}

		// Note: Apparently tripwire isn't rendered in the shadow pass.
		// worldRenderer.invokeRenderType(RenderType.getTripwire(), modelView, cameraX, cameraY, cameraZ);

		debugStringTerrain = ((RenderGlobal) worldRenderer).getDebugInfoRenders();

		profiler.endStartSection("generate mipmaps");

		generateMipmaps();

		profiler.endStartSection("restore gl state");

		restoreGlState();

		if (worldRenderer instanceof CullingDataCache) {
			((CullingDataCache) worldRenderer).restoreState();
		}

		ACTIVE = false;
		profiler.endSection();
		profiler.endStartSection("updatechunks");
	}

	public void addDebugText(List<String> messages) {
		if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
			messages.add("[" + Iris.MODNAME + "] Shadow Maps: off, shadow distance 0");
			return;
		}

		messages.add("[" + Iris.MODNAME + "] Shadow Maps: " + debugStringOverall);
		messages.add("[" + Iris.MODNAME + "] Shadow Distance Terrain: " + terrainFrustumHolder.getDistanceInfo() + " Entity: " + entityFrustumHolder.getDistanceInfo());
		messages.add("[" + Iris.MODNAME + "] Shadow Culling Terrain: " + terrainFrustumHolder.getCullingInfo() + " Entity: " + entityFrustumHolder.getCullingInfo());
		messages.add("[" + Iris.MODNAME + "] Shadow Terrain: " + debugStringTerrain
				+ (shouldRenderTerrain ? "" : " (no terrain) ") + (shouldRenderTranslucent ? "" : "(no translucent)"));
		messages.add("[" + Iris.MODNAME + "] Shadow Entities: " + getEntitiesDebugString());
		messages.add("[" + Iris.MODNAME + "] Shadow Block Entities: " + getBlockEntitiesDebugString());

		// todo
//		if (buffers instanceof DrawCallTrackingRenderBuffers && (shouldRenderEntities || shouldRenderPlayer)) {
//			DrawCallTrackingRenderBuffers drawCallTracker = (DrawCallTrackingRenderBuffers) buffers;
//			messages.add("[" + Iris.MODNAME + "] Shadow Entity Batching: " + BatchingDebugMessageHelper.getDebugMessage(drawCallTracker));
//		}
	}

	private String getEntitiesDebugString() {
		return (shouldRenderEntities || shouldRenderPlayer) ? (renderedShadowEntities + "/" + Minecraft.getMinecraft().world.getLoadedEntityList().size()) : "disabled by pack";
	}

	private String getBlockEntitiesDebugString() {
		return shouldRenderBlockEntities ? (renderedShadowBlockEntities + "/" + Minecraft.getMinecraft().world.loadedTileEntityList.size()) : "disabled by pack";
	}

	private static class MipmapPass {
		private final int texture;
		private final int targetFilteringMode;

		public MipmapPass(int texture, int targetFilteringMode) {
			this.texture = texture;
			this.targetFilteringMode = targetFilteringMode;
		}

		public int getTexture() {
			return texture;
		}

		public int getTargetFilteringMode() {
			return targetFilteringMode;
		}
	}
}
