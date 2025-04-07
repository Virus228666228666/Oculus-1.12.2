package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_TICK;

import java.util.Objects;

import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureInfoCache.TextureInfo;
import net.coderbot.iris.texture.TextureTracker;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import net.coderbot.iris.uniforms.transforms.SmoothedVec2f;
import net.coderbot.iris.vendored.joml.Math;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vendored.joml.Vector4i;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;

public final class CommonUniforms {
	private static final Minecraft client = Minecraft.getMinecraft();
	private static final Vector2i ZERO_VECTOR_2i = new Vector2i();
	private static final Vector4i ZERO_VECTOR_4i = new Vector4i(0, 0, 0, 0);
	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	private CommonUniforms() {
		// no construction allowed
	}

	// Needs to use a LocationalUniformHolder as we need it for the common uniforms
	public static void addCommonUniforms(DynamicUniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		CameraUniforms.addCameraUniforms(uniforms, updateNotifier);
		ViewportUniforms.addViewportUniforms(uniforms);
		WorldTimeUniforms.addWorldTimeUniforms(uniforms);
		SystemTimeUniforms.addSystemTimeUniforms(uniforms);
		new CelestialUniforms(directives.getSunPathRotation()).addCelestialUniforms(uniforms);
		IdMapUniforms.addIdMapUniforms(updateNotifier, uniforms, idMap, directives.isOldHandLight());
		IrisExclusiveUniforms.addIrisExclusiveUniforms(uniforms);
		MatrixUniforms.addMatrixUniforms(uniforms, directives);
		HardcodedCustomUniforms.addHardcodedCustomUniforms(uniforms, updateNotifier);
		FogUniforms.addFogUniforms(uniforms);

		// TODO: OptiFine doesn't think that atlasSize is a "dynamic" uniform,
		//       but we do. How will custom uniforms depending on atlasSize work?
		uniforms.uniform2i("atlasSize", () -> {
			int glId = GlStateManagerAccessor.getTEXTURES()[0].textureName;

			AbstractTexture texture = TextureTracker.INSTANCE.getTexture(glId);
			if (texture instanceof TextureMap) {
				TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
				return new Vector2i(info.getWidth(), info.getHeight());
			}

			return ZERO_VECTOR_2i;
		}, StateUpdateNotifiers.bindTextureNotifier);

		uniforms.uniform2i("gtextureSize", () -> {
			int glId = GlStateManagerAccessor.getTEXTURES()[0].textureName;

			TextureInfo info = TextureInfoCache.INSTANCE.getInfo(glId);
			return new Vector2i(info.getWidth(), info.getHeight());

		}, StateUpdateNotifiers.bindTextureNotifier);

		uniforms.uniform4i("blendFunc", () -> {
			GlStateManager.BlendState blend = GlStateManagerAccessor.getBLEND();

			if (((BooleanStateAccessor) blend.blend).isEnabled()) {
				return new Vector4i(blend.srcFactor, blend.dstFactor, blend.srcFactorAlpha, blend.dstFactorAlpha);
			} else {
				return ZERO_VECTOR_4i;
			}
		}, StateUpdateNotifiers.blendFuncNotifier);

		uniforms.uniform1i("renderStage", () -> GbufferPrograms.getCurrentPhase().ordinal(), StateUpdateNotifiers.phaseChangeNotifier);

		CommonUniforms.generalCommonUniforms(uniforms, updateNotifier, directives);
	}

	public static void generalCommonUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives) {
		ExternallyManagedUniforms.addExternallyManagedUniforms116(uniforms);

		SmoothedVec2f eyeBrightnessSmooth = new SmoothedVec2f(directives.getEyeBrightnessHalfLife(), directives.getEyeBrightnessHalfLife(), CommonUniforms::getEyeBrightness, updateNotifier);

		uniforms
			.uniform1b(PER_FRAME, "hideGUI", () -> client.gameSettings.hideGUI)
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness)
			.uniform1f(PER_FRAME, "nightVision", CommonUniforms::getNightVision)
			.uniform1b(PER_FRAME, "is_sneaking", CommonUniforms::isSneaking)
			.uniform1b(PER_FRAME, "is_sprinting", CommonUniforms::isSprinting)
			.uniform1b(PER_FRAME, "is_hurt", CommonUniforms::isHurt)
			.uniform1b(PER_FRAME, "is_invisible", CommonUniforms::isInvisible)
			.uniform1b(PER_FRAME, "is_burning", CommonUniforms::isBurning)
			.uniform1b(PER_FRAME, "is_on_ground", CommonUniforms::isOnGround)
			// TODO: Do we need to clamp this to avoid fullbright breaking shaders? Or should shaders be able to detect
			//       that the player is trying to turn on fullbright?
			.uniform1f(PER_FRAME, "screenBrightness", () -> client.gameSettings.gammaSetting)
			// just a dummy value for shaders where entityColor isn't supplied through a vertex attribute (and thus is
			// not available) - suppresses warnings. See AttributeShaderTransformer for the actual entityColor code.
			.uniform4f(ONCE, "entityColor", () -> new Vector4f(0, 0, 0, 0))
			.uniform1f(ONCE, "pi", () -> Math.PI)
//			.uniform1f(PER_TICK, "playerMood", CommonUniforms::getPlayerMood)
			.uniform2i(PER_FRAME, "eyeBrightness", CommonUniforms::getEyeBrightness)
			.uniform2i(PER_FRAME, "eyeBrightnessSmooth", () -> {
				Vector2f smoothed = eyeBrightnessSmooth.get();
				return new Vector2i((int) smoothed.x(),(int) smoothed.y());
			})
			.uniform1f(PER_TICK, "rainStrength", CommonUniforms::getRainStrength)
			.uniform1f(PER_TICK, "wetness", new SmoothedFloat(directives.getWetnessHalfLife(), directives.getDrynessHalfLife(), CommonUniforms::getRainStrength, updateNotifier))
			.uniform3d(PER_FRAME, "skyColor", CommonUniforms::getSkyColor)
			.uniform3d(PER_FRAME, "fogColor", CapturedRenderingState.INSTANCE::getFogColor);
	}

	private static boolean isOnGround() {
		return client.player != null && client.player.onGround;
	}

	private static boolean isHurt() {
		if (client.player != null) {
			return client.player.hurtTime > 0; // Do not use isHurt, that's not what we want!
		} else {
			return false;
		}
	}

	private static boolean isInvisible() {
		if (client.player != null) {
			return client.player.isInvisible();
		} else {
			return false;
		}
	}

	private static boolean isBurning() {
		if (client.player != null) {
			return client.player.isBurning();
		} else {
			return false;
		}
	}

	private static boolean isSneaking() {
		if (client.player != null) {
			return client.player.isSneaking();
		} else {
			return false;
		}
	}

	private static boolean isSprinting() {
		if (client.player != null) {
			return client.player.isSprinting();
		} else {
			return false;
		}
	}

	private static Vector3d getSkyColor() {
		if (client.world == null || client.getRenderViewEntity() == null) {
			return ZERO_VECTOR_3d;
		}

		return JomlConversions.fromVec3(client.world.getSkyColor(client.getRenderViewEntity(),
				CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	static float getBlindness() {
		Entity cameraEntity = client.getRenderViewEntity();

		if (cameraEntity instanceof EntityLiving) {
			PotionEffect blindness = ((EntityLiving) cameraEntity).getActivePotionEffect(MobEffects.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in BackgroundRenderer
				// TODO: Add this to ShaderDoc
				return Math.clamp(0.0F, 1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

//	private static float getPlayerMood() {
//		if (!(client.getRenderViewEntity() instanceof EntityPlayerSP)) {
//			return 0.0F;
//		}
//
//		return Math.clamp(getCurrentMood((EntityPlayerSP) client.getRenderViewEntity()), 0.0F, 1.0F);
//	}

	static float getRainStrength() {
		if (client.world == null) {
			return 0f;
		}

		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			client.world.getRainStrength(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static Vector2i getEyeBrightness() {
		if (client.getRenderViewEntity() == null || client.world == null) {
			return ZERO_VECTOR_2i;
		}

		Vec3d feet = client.getRenderViewEntity().getPositionVector();
		Vec3d eyes = new Vec3d(feet.x, client.getRenderViewEntity().getEyeHeight(), feet.z);
		BlockPos eyeBlockPos = new BlockPos(eyes);

		int blockLight = client.world.getLightFor(EnumSkyBlock.BLOCK, eyeBlockPos);
		int skyLight = client.world.getLightFor(EnumSkyBlock.SKY, eyeBlockPos);

		return new Vector2i(blockLight * 16, skyLight * 16);
	}

	private static float getNightVision() {
		Entity cameraEntity = client.getRenderViewEntity();

		if (cameraEntity instanceof EntityLiving) {
			EntityLiving livingEntity = (EntityLiving) cameraEntity;

			try {
				// See MixinGameRenderer#iris$safecheckNightvisionStrength.
				//
				// We modify the behavior of getNightVisionScale so that it's safe for us to call it even on entities
				// that don't have the effect, allowing us to pick up modified night vision strength values from mods
				// like Origins.
				//
				// See: https://github.com/apace100/apoli/blob/320b0ef547fbbf703de7154f60909d30366f6500/src/main/java/io/github/apace100/apoli/mixin/GameRendererMixin.java#L153
				float nightVisionStrength =
						Minecraft.getMinecraft().entityRenderer.getNightVisionBrightness(livingEntity, CapturedRenderingState.INSTANCE.getTickDelta());

				if (nightVisionStrength > 0) {
					// Just protecting against potential weird mod behavior
					return Math.clamp(0.0F, 1.0F, nightVisionStrength);
				}
			} catch (NullPointerException e) {
				// If our injection didn't get applied, a NullPointerException will occur from calling that method if
				// the entity doesn't currently have night vision. This isn't pretty but it's functional.
				return 0.0F;
			}
		}

		return 0.0F;
	}

	static int isEyeInWater() {
		// Note: With certain utility / cheat mods, this method will return air even when the player is submerged when
		// the "No Overlay" feature is enabled.
		//
		// I'm not sure what the best way to deal with this is, but the current approach seems to be an acceptable one -
		// after all, disabling the overlay results in the intended effect of it not really looking like you're
		// underwater on most shaderpacks. For now, I will leave this as-is, but it is something to keep in mind.
		if (client.player.isInsideOfMaterial(Material.WATER)) {
			return 1;
		} else if (client.player.isInsideOfMaterial(Material.LAVA)) {
			return 2;
		} else {
			return 0;
		}
	}

	static {
		GbufferPrograms.init();
	}
}
