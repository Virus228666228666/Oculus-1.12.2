package net.coderbot.iris.mixin;

import net.coderbot.iris.pipeline.HandRenderer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemInHandRenderer {
	@Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At("HEAD"), cancellable = true)
	private void iris$skipTranslucentHands(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo ci) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			if (HandRenderer.INSTANCE.isRenderingSolid() && HandRenderer.INSTANCE.isHandTranslucent(hand)) {
				ci.cancel();
			} else if (!HandRenderer.INSTANCE.isRenderingSolid() && !HandRenderer.INSTANCE.isHandTranslucent(hand)) {
				ci.cancel();
			}
		}
	}
}
