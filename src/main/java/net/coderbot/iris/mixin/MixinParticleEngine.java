package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures that all particles are rendered with the textured_lit shader program.
 */
// TODO not sure about lit
@Mixin(EntityRenderer.class)
public class MixinParticleEngine {
	@Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderLitParticles(Lnet/minecraft/entity/Entity;F)V", shift = At.Shift.BEFORE))
	private void iris$beginDrawingParticles(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setPhase(WorldRenderingPhase.PARTICLES));
	}

	@Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lnet/minecraft/entity/Entity;F)V", shift = At.Shift.AFTER))
	private void iris$finishDrawingParticles(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.setPhase(WorldRenderingPhase.NONE));
	}
}
