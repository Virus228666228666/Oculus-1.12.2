package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.coderbot.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.coderbot.batchedentityrendering.impl.Groupable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
	@Unique
	private Groupable groupable;

	@Inject(method = "renderEntities", at = @At("HEAD"))
	private void ber$beginRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
		EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
		if (renderer instanceof DrawCallTrackingRenderBuffers) {
			((DrawCallTrackingRenderBuffers) renderer).resetDrawCounts();
		}
		if (renderer instanceof Groupable) {
			groupable = (Groupable) renderer;
		}
	}

	/*@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V"))
	private void ber$beforeRenderEntity(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
		if (groupable != null) {
			groupable.startGroup();
		}
	}

	@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V", shift = At.Shift.AFTER))
	private void ber$afterRenderEntity(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
		if (groupable != null) {
			groupable.endGroup();
		}
	}

	@Inject(method = "renderEntities", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void ber$beginTranslucents(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
		Minecraft.getMinecraft().profiler.endStartSection("entity_draws");
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/entity/default.png"));
	}*/

	@Inject(method = "renderEntities", at = @At("RETURN"))
	private void ber$endRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
		groupable = null;
	}
}
