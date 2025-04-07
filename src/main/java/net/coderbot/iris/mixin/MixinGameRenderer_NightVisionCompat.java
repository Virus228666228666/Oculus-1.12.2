package net.coderbot.iris.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Priority of 1010 (> 1000) to run after other mod mixins. In particular, Night Vision Flash Be Gone overwrites this
// method, so we need to run after it so that our injection silently fails instead of crashing the game.
@Mixin(value = EntityRenderer.class, priority = 1010)
public class MixinGameRenderer_NightVisionCompat {
	// Origins compatibility: Allows us to call getNightVisionScale even if the entity does not have night vision.
	// This injection gives a chance for mods injecting at HEAD to return a modified night vision value.
	//
	// It's optional because of Night Vision Flash Be Gone overwriting this method, but having this injection
	// succeed avoids a lot of spurious (but silently caught) NullPointerExceptions.
	@Inject(method = "getNightVisionBrightness", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/potion/PotionEffect;getDuration()I"), cancellable = true,
			require = 0)
	private void iris$safecheckNightvisionStrength(EntityLivingBase livingEntity, float partialTicks,
												   CallbackInfoReturnable<Float> cir){
		if (livingEntity.getActivePotionEffect(MobEffects.NIGHT_VISION) == null) {
			cir.setReturnValue(0.0f);
		}
	}
}
