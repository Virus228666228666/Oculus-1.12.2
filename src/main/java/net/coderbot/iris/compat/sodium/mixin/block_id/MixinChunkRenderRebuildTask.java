package net.coderbot.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.pipeline.FluidRenderer;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.minecraft.client.renderer.chunk.VisGraph;

/**
 * Passes additional information indirectly to the vertex writer to support the mc_Entity and at_midBlock parts of the vertex format.
 */
@Mixin(ChunkRenderRebuildTask.class)
public class MixinChunkRenderRebuildTask {
	// todo: unable to locate obfuscation mapping for ... target performBuild
//	@Inject(method = "performBuild", at = @At(value = "INVOKE",
//			target = "Lme/jellysquid/mods/sodium/common/util/WorldUtil;toFluidBlock(Lnet/minecraft/block/Block;)Lnet/minecraftforge/fluids/IFluidBlock;", ordinal = 0),
//			locals = LocalCapture.CAPTURE_FAILHARD)
//	private void iris$setLocalPos(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
//								  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult<?>> cir,
//								  ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds,
//								  WorldSlice slice, int baseX, int baseY, int baseZ,
//								  BlockPos.MutableBlockPos pos, BlockPos renderOffset,
//								  int relY, int relZ, int relX) {
//		if (buffers instanceof ChunkBuildBuffersExt) {
//			((ChunkBuildBuffersExt) buffers).iris$setLocalPos(relX, relY, relZ);
//		}
//	}

	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/BlockModelShapes;getModelForState(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/block/model/IBakedModel;"))
	private IBakedModel iris$wrapGetBlockLayer(BlockModelShapes instance, IBlockState blockState, ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers) {
		if (buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(blockState, ExtendedDataHelper.BLOCK_RENDER_TYPE);
		}

		return instance.getModelForState(blockState);
	}

//	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
//			target = "Lme/jellysquid/mods/sodium/client/render/pipeline/FluidRenderer;render(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuffers;)Z"))
//	private boolean iris$wrapGetFluidLayer(FluidRenderer renderer, IBlockAccess world, IBlockState fluidState, BlockPos pos, ChunkModelBuffers modelBuffers, ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers) {
//		if (buffers instanceof ChunkBuildBuffersExt) {
//			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(fluidState, ExtendedDataHelper.FLUID_RENDER_TYPE);
//		}
//
//		return renderer.render(world, fluidState, pos, modelBuffers);
//	}

//	@Inject(method = "performBuild",
//			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasTileEntity(Lnet/minecraft/block/state/IBlockState;)Z"))
//	private void iris$resetContext(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
//							  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult<?>> cir) {
//		if (buffers instanceof ChunkBuildBuffersExt) {
//			((ChunkBuildBuffersExt) buffers).iris$resetBlockContext();
//		}
//	}
}
