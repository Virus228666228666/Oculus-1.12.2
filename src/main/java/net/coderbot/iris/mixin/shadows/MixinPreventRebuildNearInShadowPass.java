package net.coderbot.iris.mixin.shadows;

import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Prevent nearby chunks from being rebuilt on the main thread in the shadow pass. Aside from causing  FPS to tank,
 * this also causes weird chunk corruption! It's critical to make sure that it's disabled as a result.
 *
 * This patch is not relevant with Sodium installed since Sodium has a completely different build path for terrain
 * setup.
 *
 * Uses a priority of 1010 to apply after Sodium's overwrite, to allow for the Group behavior to activate. Otherwise,
 * if we apply with the same priority, then we'll just get a Mixin error due to the injects conflicting with the
 * {@code @Overwrite}. Using {@code @Group} allows us to avoid a fragile Mixin plugin.
 */
@Mixin(value = RenderGlobal.class, priority = 1010)
public class MixinPreventRebuildNearInShadowPass {
	@Shadow
	private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos;

	@Group(name = "iris_MixinPreventRebuildNearInShadowPass", min = 1, max = 1)
	@Inject(method = "setupTerrain",
			at = @At(value = "INVOKE_STRING",
					target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
					args = "ldc=rebuildNear"),
			cancellable = true,
			require = 0)
	private void iris$preventRebuildNearInShadowPass(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
		if (ShadowRenderer.ACTIVE) {
			for (RenderGlobal.ContainerLocalRenderInformation chunk : this.renderInfos) {
				ShadowRenderer.visibleBlockEntities.addAll(((ChunkInfoAccessor) chunk).getChunk().getCompiledChunk().getTileEntities());
			}
			Minecraft.getMinecraft().profiler.endSection();
			ci.cancel();
		}
	}

	@Group(name = "iris_MixinPreventRebuildNearInShadowPass", min = 1, max = 1)
	@Inject(method = "setupTerrain",
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/client/gl/device/RenderDevice;enterManagedCode()V",
					remap = false),
			require = 0)
	private void iris$cannotInject(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
		// Dummy injection just to assert that either Sodium is present, or the vanilla injection passed.
	}
}
