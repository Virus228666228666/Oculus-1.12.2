package net.coderbot.iris.mixin.state_tracking;

import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.Iris;

@Mixin(ShaderGroup.class)
public class MixinPostChain {
	@Inject(method = "render(F)V", at = @At("HEAD"))
	private void iris$beforeProcess(float f, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.getRenderTargetStateListener().beginPostChain());
	}

	@Inject(method = "render(F)V", at = @At("RETURN"))
	private void iris$afterProcess(float f, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.getRenderTargetStateListener().endPostChain());
	}
}
