package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinScreenEffectRenderer {
	@Inject(method = "renderWaterOverlayTexture", at = @At(value = "HEAD"), cancellable = true)
	private void iris$disableUnderWaterOverlayRendering(float partialTicks, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && !pipeline.shouldRenderUnderwaterOverlay()) {
			ci.cancel();
		}
	}
}
