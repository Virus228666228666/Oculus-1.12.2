package net.coderbot.iris.mixin;

import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.Iris;

@Mixin(value = GameSettings.class, priority = 990)
public class MixinOptions_Entrypoint {
	@Unique
	private static boolean iris$initialized;

	@Inject(method = "loadOptions", at = @At("HEAD"))
	private void iris$beforeLoadOptions(CallbackInfo ci) {
		if (iris$initialized) {
			return;
		}

		iris$initialized = true;
		new Iris().onEarlyInitialize();
	}
}
