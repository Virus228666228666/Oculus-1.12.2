package net.coderbot.iris.mixin;

import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.gl.blending.BlendModeStorage;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_BlendOverride {
	@Inject(method = "disableBlend", at = @At("HEAD"), cancellable = true)
	private static void iris$blendDisableLock(CallbackInfo ci) {
		if (BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendModeToggle(false);
			ci.cancel();
		}
	}

	@Inject(method = "enableBlend", at = @At("HEAD"), cancellable = true)
	private static void iris$blendEnableLock(CallbackInfo ci) {
		if(BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendModeToggle(true);
			ci.cancel();
		}
	}

	@Inject(method = "blendFunc(II)V", at = @At("HEAD"), cancellable = true)
	private static void iris$blendFuncLock(int srcFactor, int dstFactor, CallbackInfo ci) {
		if(BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendFunc(srcFactor, dstFactor, srcFactor, dstFactor);
			ci.cancel();
		}
	}

	@Inject(method = "tryBlendFuncSeparate(IIII)V", at = @At("HEAD"), cancellable = true)
	private static void iris$blendFuncSeparateLock(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha, CallbackInfo ci) {
		if(BlendModeStorage.isBlendLocked()) {
			BlendModeStorage.deferBlendFunc(srcRgb, dstRgb, srcAlpha, dstAlpha);
			ci.cancel();
		}
	}
}
