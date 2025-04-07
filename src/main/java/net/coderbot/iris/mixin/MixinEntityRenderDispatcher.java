package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public class MixinEntityRenderDispatcher {

	@Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
	private void iris$maybeSuppressEntityShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks, CallbackInfo ci) {
		iris$maybeSuppressShadow(ci);
	}

	// The underlying method called by renderShadow.
	@Inject(method = "renderShadowSingle", at = @At("HEAD"), cancellable = true)
	private void renderBlockShadow(IBlockState state, double x, double y, double z, BlockPos pos, float shadowAlpha, float shadowSize, double x2, double y2, double z2, CallbackInfo ci) {
		iris$maybeSuppressShadow(ci);
	}

	@Unique
	private static void iris$maybeSuppressShadow(CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && pipeline.shouldDisableVanillaEntityShadows()) {
			ci.cancel();
		}
	}
}
