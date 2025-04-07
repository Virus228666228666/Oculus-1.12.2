package net.coderbot.iris.mixin;

import nanolive.utils.RenderGlobalExtended;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal implements RenderGlobalExtended {
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/RenderGlobal;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";

	@Unique
	private WorldRenderingPipeline pipeline;

	/*@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_SKY))
	private void iris$beginSky(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		// Use CUSTOM_SKY until levelFogColor is called as a heuristic to catch FabricSkyboxes.
		pipeline.setPhase(WorldRenderingPhase.CUSTOM_SKY);
	}*/

	@Inject(method = "renderSky(FI)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFF)V", ordinal = 0))
	private void iris$renderSky$beginNormalSky(float partialTicks, int pass, CallbackInfo ci) {
		// None of the vanilla sky is rendered until after this call, so if anything is rendered before, it's
		// CUSTOM_SKY.
		pipeline.setPhase(WorldRenderingPhase.SKY);
	}

	@Inject(method = "renderSky(FI)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;SUN_TEXTURES:Lnet/minecraft/util/ResourceLocation;"))
	private void iris$setSunRenderStage(float partialTicks, int pass, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.SUN);
	}

	@Inject(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;calcSunriseSunsetColors(FF)[F"))
	private void iris$setSunsetRenderStage(float partialTicks, int pass, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.SUNSET);
	}

	@Inject(method = "renderSky(FI)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;MOON_PHASES_TEXTURES:Lnet/minecraft/util/ResourceLocation;"))
	private void iris$setMoonRenderStage(float partialTicks, int pass, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.MOON);
	}

	@Inject(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getStarBrightness(F)F"))
	private void iris$setStarRenderStage(float partialTicks, int pass, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.STARS);
	}

	@Inject(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;"))
	private void iris$setVoidRenderStage(float partialTicks, int pass, CallbackInfo ci) {
		pipeline.setPhase(WorldRenderingPhase.VOID);
	}

//	@Inject(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getTimeOfDay(F)F"),
//		slice = @Slice(from = @At(value = "FIELD", target = "com/mojang/math/Vector3f.YP : Lcom/mojang/math/Vector3f;")))
//	private void iris$renderSky$tiltSun(float partialTicks, int pass, CallbackInfo ci) {
//		poseStack.mulPose(Vector3f.ZP.rotationDegrees(pipeline.getSunPathRotation()));
//	}

	@Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"))
	private void iris$beginTerrainLayer(BlockRenderLayer renderLayer, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> cir) {
		pipeline.setPhase(WorldRenderingPhase.fromTerrainRenderType(renderLayer));
	}

	@Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("RETURN"))
	private void iris$endTerrainLayer(BlockRenderLayer renderLayer, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> cir) {
		pipeline.setPhase(WorldRenderingPhase.NONE);
	}

//	@ModifyArg(method = RENDER_WEATHER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V", ordinal = 0))
//	private boolean iris$writeRainAndSnowToDepthBuffer(boolean depthMaskEnabled) {
//		if (pipeline.shouldWriteRainAndSnowToDepthBuffer()) {
//			return true;
//		}
//
//		return depthMaskEnabled;
//	}

//	@ModifyArg(method = "renderLevel",
//		at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/MultiBufferSource$BufferSource.getBuffer (Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
//		slice = @Slice(
//			from = @At(value = "CONSTANT", args = "stringValue=outline"),
//			to = @At(value = "INVOKE", target = "net/minecraft/client/renderer/LevelRenderer.renderHitOutline (Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
//		))
//	private RenderType iris$beginBlockOutline(RenderType type) {
//		return OuterWrappedRenderType.wrapExactlyOnce("iris:is_outline", type, IsOutlineRenderStateShard.INSTANCE);
//	}

//	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent"))
//	private void iris$beginTranslucents(PoseStack poseStack, float tickDelta, long limitTime,
//										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
//										LightTexture lightTexture, Matrix4f projection,
//										CallbackInfo ci) {
//		pipeline.beginHand();
//		HandRenderer.INSTANCE.renderSolid(tickDelta, gameRenderer);
//		Minecraft.getInstance().getProfiler().popPush("iris_pre_translucent");
//		pipeline.beginTranslucents();
//	}

	@Override
	public WorldRenderingPipeline getPipeline() {
		return pipeline;
	}

	@Override
	public void setPipeline(WorldRenderingPipeline pipeline) {
		this.pipeline = pipeline;
	}
}
