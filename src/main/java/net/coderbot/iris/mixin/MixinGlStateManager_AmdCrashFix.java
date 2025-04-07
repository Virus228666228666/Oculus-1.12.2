package net.coderbot.iris.mixin;

import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Works around a bug in AMD drivers that causes crashes with glShaderSource.
 *
 * See @link ShaderWorkarounds#safeShaderSource(int, ByteBuffer) for more details.
 */
@Mixin(GlStateManager.class)
public class MixinGlStateManager_AmdCrashFix {
	// might not be needed in 1.12.2?
	// if it is, then glShaderSource exists both in GL20 and in OpenGlHelper (but with int, ByteBuffer)
//	@Redirect(method = "glShaderSource(ILjava/lang/CharSequence;)V",
//			at = @At(value = "INVOKE",
//					target = "org/lwjgl/opengl/GL20.glShaderSource (ILjava/lang/CharSequence;)V"))
//	private static void iris$safeShaderSource(int glId, CharSequence source) {
//		ShaderWorkarounds.safeShaderSource(glId, source);
//	}
}
