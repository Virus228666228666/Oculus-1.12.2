package net.coderbot.iris.mixin.vertices.immediate;

import net.coderbot.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Uses a priority of 999 to apply before the main Iris mixins to draw entities before deferred runs.
@Mixin(value = EntityRenderer.class, priority = 999)
public class MixinLevelRenderer {
	@Inject(method = "renderWorld", at = @At("HEAD"))
	private void iris$immediateStateBeginLevelRender(float partialTicks, long finishTimeNano, CallbackInfo ci) {
		ImmediateState.isRenderingLevel = true;
	}

	@Inject(method = "renderWorld", at = @At("RETURN"))
	private void iris$immediateStateEndLevelRender(float partialTicks, long finishTimeNano, CallbackInfo ci) {
		ImmediateState.isRenderingLevel = false;
	}
}
