package net.coderbot.iris.mixin.vertices.block_rendering;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.BufferBuilder;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.RenderChunk;

/**
 * Captures and tracks the current block being rendered.
 *
 * Uses a priority of 999 so that we apply before Indigo's mixins.
 */
@Mixin(value = RenderChunk.class, priority = 999)
public class MixinChunkRebuildTask {
	@Unique
	private static final String RENDER = "rebuildChunk(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V";

	@Unique
	private BlockSensitiveBufferBuilder lastBufferBuilder;

	// Resolve the ID map on the main thread to avoid thread safety issues
	@Unique
	private final Object2IntMap<IBlockState> blockStateIds = getBlockStateIds();

	@Unique
	private Object2IntMap<IBlockState> getBlockStateIds() {
		return BlockRenderingSettings.INSTANCE.getBlockStateIds();
	}

	@Unique
	private short resolveBlockId(IBlockState state) {
		if (blockStateIds == null) {
			return -1;
		}

		return blockStateIds.getOrDefault(state, -1).shortValue();
	}
// todo: these injects aren't working and I don't know why
//	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/material/FluidState;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
//	private void iris$onRenderLiquid(float cameraX, float cameraY, float cameraZ, CompiledChunk data, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<Set<BlockEntity>> cir, int i, BlockPos blockPos, BlockPos blockPos2, VisGraph chunkOcclusionDataBuilder, Set<BlockEntity> set, RenderChunkRegion chunkRendererRegion, PoseStack poseStack, Random random, BlockRenderDispatcher blockRenderManager, Iterator<BlockPos> var15, BlockPos pos, BlockState blockState, Block block, FluidState fluidState, IModelData modelData, Iterator iterator, RenderType renderType, BufferBuilder bufferBuilder) {
//		if (bufferBuilder instanceof BlockSensitiveBufferBuilder) {
//			lastBufferBuilder = ((BlockSensitiveBufferBuilder) bufferBuilder);
//			// TODO: We're using createLegacyBlock? That seems like something that Mojang wants to deprecate.
//			lastBufferBuilder.beginBlock(resolveBlockId(fluidState.createLegacyBlock()), ExtendedDataHelper.FLUID_RENDER_TYPE, pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
//		}
//	}
//
//	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderLiquid(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/material/FluidState;)Z", shift = At.Shift.AFTER))
//	private void iris$finishRenderingLiquid(float cameraX, float cameraY, float cameraZ, CompiledChunk data, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<Set<BlockEntity>> cir) {
//		if (lastBufferBuilder != null) {
//			lastBufferBuilder.endBlock();
//			lastBufferBuilder = null;
//		}
//	}
//
//	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderModel(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
//	private void iris$onRenderBlock(float cameraX, float cameraY, float cameraZ, CompiledChunk data, ChunkBufferBuilderPack buffers, CallbackInfoReturnable<Set<BlockEntity>> cir, int i, BlockPos blockPos, BlockPos blockPos2, VisGraph chunkOcclusionDataBuilder, Set<BlockEntity> set, RenderChunkRegion chunkRendererRegion, PoseStack poseStack, Random random, BlockRenderDispatcher blockRenderManager, Iterator<BlockPos> posIterator, BlockPos pos, BlockState blockState, Block block, FluidState fluidState, IModelData modelData, Iterator iterator, RenderType renderType, RenderType renderType2, BufferBuilder bufferBuilder) {
//		if (bufferBuilder instanceof BlockSensitiveBufferBuilder) {
//			lastBufferBuilder = ((BlockSensitiveBufferBuilder) bufferBuilder);
//			lastBufferBuilder.beginBlock(resolveBlockId(blockState), ExtendedDataHelper.BLOCK_RENDER_TYPE, pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
//		}
//	}

	// public boolean renderModel(net.minecraft.world.IBlockAccess; blockAccessIn,
	//                           net.minecraft.client.renderer.block.model.IBakedModel; modelIn,
	//                           net.minecraft.block.state.IBlockState; blockStateIn,
	//                           net.minecraft.util.math.BlockPos; blockPosIn,
	//                           net.minecraft.client.renderer.BufferBuilder; buffer,
	//                           Z boolean checkSides)
//	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModel(" +
//			"Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;" +
//			"Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;Z)Z", shift = At.Shift.AFTER))
//	private void iris$finishRenderingBlock(float cameraX, float cameraY, float cameraZ, ChunkCompileTaskGenerator generator, CallbackInfoReturnable<Void> cir) {
//		if (lastBufferBuilder != null) {
//			lastBufferBuilder.endBlock();
//			lastBufferBuilder = null;
//		}
//	}
}
