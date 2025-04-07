package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinFogRenderer {
	@Shadow private float fogColorRed, fogColorGreen, fogColorBlue;

	@Inject(method = "updateFogColor", at = @At("TAIL"))
	private void render(float partialTicks, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setFogColor(fogColorRed, fogColorGreen, fogColorBlue);
	}
}
