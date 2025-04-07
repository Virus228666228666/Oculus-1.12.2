package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class MixinTextureManager {
	@Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/ProgressManager;pop(Lnet/minecraftforge/fml/common/ProgressManager$ProgressBar;)V", shift = At.Shift.BEFORE))
	private void iris$onTailReloadLambda(IResourceManager resourceManager, CallbackInfo ci) {
		TextureFormatLoader.reload(resourceManager);
		PBRTextureManager.INSTANCE.clear();
	}

	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void iris$reloadResources(IResourceManager resourceManager, CallbackInfo ci) {
		PBRTextureManager.INSTANCE.clear();
	}
}
