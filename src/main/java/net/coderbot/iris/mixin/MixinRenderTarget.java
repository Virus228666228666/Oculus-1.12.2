package net.coderbot.iris.mixin;

import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(Framebuffer.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {

	private int iris$depthBufferVersion;
	private int iris$colorBufferVersion;

	@Inject(method = "deleteFramebuffer", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}
}
