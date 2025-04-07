package net.coderbot.iris.mixin.sky;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinLevelRenderer_SunMoonToggle {
	@Inject(
			method = "renderSky(FI)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V",
					ordinal = 2,
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	private void iris$cancelSunDraw(float partialTicks, int pass, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			ci.cancel();
		}
	}

	@Inject(
			method = "renderSky(FI)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V",
					ordinal = 3,
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	private void iris$cancelMoonDraw(float partialTicks, int pass, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			ci.cancel();
		}
	}
}
