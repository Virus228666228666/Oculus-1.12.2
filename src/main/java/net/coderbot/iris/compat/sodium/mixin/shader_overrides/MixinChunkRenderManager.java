package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ChunkRenderBackendExt;

@Mixin(ChunkRenderManager.class)
public class MixinChunkRenderManager {
	// todo: unable to locate obfuscation mapping
//	@Redirect(method = "renderLayer",
//			at = @At(value = "INVOKE",
//					target = "me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderBackend.begin ()V"))
//	private void iris$backendBeginExt(ChunkRenderBackend<?> backend, BlockRenderPass pass, double x, double y, double z) {
//		if (backend instanceof ChunkRenderBackendExt) {
//			((ChunkRenderBackendExt) backend).iris$begin(pass);
//		} else {
//			backend.begin();
//		}
//	}
}
