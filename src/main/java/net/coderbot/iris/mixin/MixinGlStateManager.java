package net.coderbot.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.renderer.GlStateManager;

import net.coderbot.iris.gl.sampler.SamplerLimits;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 8), require = 1)
	private static int iris$increaseMaximumAllowedTextureUnits(int existingValue) {
		return SamplerLimits.get().getMaxTextureUnits();
	}
}
