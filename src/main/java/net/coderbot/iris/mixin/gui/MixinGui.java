package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGui {
	@Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
	private void iris$disableVignetteRendering(float lightLevel, ScaledResolution scaledRes, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && !pipeline.shouldRenderVignette()) {
			// we need to set up the GUI render state ourselves if we cancel the vignette
			GlStateManager.enableDepth();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			ci.cancel();
		}
	}
}
