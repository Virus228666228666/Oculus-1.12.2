package net.coderbot.iris.mixin.sky;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables the sunrise / sunset effect when blindness is active or when submerged in a fluid.
 *
 * Inspired by https://github.com/CaffeineMC/sodium-fabric/pull/710, but this implementation
 * takes a far more conservative approach and only disables specific parts of sky rendering in high-fog
 * situations.
 */
@Mixin(WorldProvider.class)
public class MixinDimensionSpecialEffects {
	@Inject(method = "calcSunriseSunsetColors", at = @At("HEAD"), cancellable = true)
	private void iris$getSunriseColor(float timeOfDay, float partialTicks, CallbackInfoReturnable<float[]> cir) {
		Minecraft mc = Minecraft.getMinecraft();
		Entity cameraEntity = mc.getRenderViewEntity();
		boolean hasBlindness = cameraEntity instanceof EntityLivingBase
			&& ((EntityLivingBase) cameraEntity).getActivePotionEffect(MobEffects.BLINDNESS) != null;

		if (hasBlindness) {
			cir.setReturnValue(null);
		}

		// TODO getBlockStateAtEntityViewpoint is not adapted to Forge's IFluidBlock, maybe it's better to use ForgeHooks.isInsideOfMaterial?
		IBlockState submergedFluid = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, cameraEntity, partialTicks);
		Block block = submergedFluid.getBlock();

		if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
			cir.setReturnValue(null);
		}
	}
}
