package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinGameRenderer {

	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(Minecraft client, IResourceManager resourceManager, CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + OpenGlHelper.getCpu());
		Iris.logger.info("GPU: " + GlStateManager.glGetString(GL11.GL_RENDERER) + " (Supports OpenGL " + GlStateManager.glGetString(GL11.GL_VERSION) + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")");
	}

	@Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V"))
	private void disableVanillaHandRendering(ItemRenderer instance, float partialTicks) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return;
		}

		instance.renderItemInFirstPerson(partialTicks);
	}

}
