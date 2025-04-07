package net.coderbot.iris.mixin.vertices;

import java.nio.ByteBuffer;

import net.coderbot.iris.vertices.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vendored.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Dynamically and transparently extends the vanilla vertex formats with additional data
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements BlockSensitiveBufferBuilder, ExtendingBufferBuilder {
	@Unique
	private boolean extending;

	@Unique
	private boolean iris$shouldNotExtend = false;

	@Unique
	private boolean iris$isTerrain = false;

	@Shadow
	private int vertexCount;

	@Unique
	private final BufferBuilderPolygonView polygon = new BufferBuilderPolygonView();

	@Unique
	private final Vector3f normal = new Vector3f();

	@Unique
	private boolean injectNormal;

	@Unique
	private short currentBlock;

	@Unique
	private short currentRenderType;

	@Unique
	private int currentLocalPosX;

	@Unique
	private int currentLocalPosY;

	@Unique
	private int currentLocalPosZ;

	@Shadow
	private ByteBuffer byteBuffer;

	@Shadow
	private int drawMode;

	@Shadow
	private VertexFormat vertexFormat;

	@Shadow
	private @Nullable VertexFormatElement vertexFormatElement;

	@Shadow
	public abstract void begin(int drawMode, VertexFormat vertexFormat);

	@Unique
	private int nextElementByte;

	private void switchFormat(VertexFormat vertexFormat) {
		if (this.vertexFormat != vertexFormat) {
			this.vertexFormat = vertexFormat;
		}
	}

	@Shadow
	private void nextVertexFormatIndex() {};

	@Override
	public void iris$beginWithoutExtending(int drawMode, VertexFormat vertexFormat) {
		iris$shouldNotExtend = true;
		begin(drawMode, vertexFormat);
		iris$shouldNotExtend = false;
	}

	@Inject(method = "begin", at = @At("HEAD"))
	private void iris$onBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		boolean shouldExtend = (!iris$shouldNotExtend) && BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat();
		extending = shouldExtend && (format == DefaultVertexFormats.BLOCK || format == DefaultVertexFormats.ITEM
				|| format == DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		vertexCount = 0;

		if (extending) {
			injectNormal = format == DefaultVertexFormats.POSITION_TEX_LMAP_COLOR;
		}
	}

	@Inject(method = "begin", at = @At("RETURN"))
	private void iris$afterBegin(int drawMode, VertexFormat format, CallbackInfo ci) {
		if (extending) {
			if (format == DefaultVertexFormats.ITEM) {
				this.switchFormat(IrisVertexFormats.ENTITY);
				this.iris$isTerrain = false;
			} else {
				this.switchFormat(IrisVertexFormats.TERRAIN);
				this.iris$isTerrain = true;
			}
			this.vertexFormatElement = this.vertexFormat.getElements().get(0);
		}
	}

	@Inject(method = "endVertex", at = @At("HEAD"))
	private void iris$beforeNext(CallbackInfo ci) {
		if (!extending) {
			return;
		}

		if (injectNormal && vertexFormatElement == DefaultVertexFormats.NORMAL_3B) {
			this.putInt(0, 0);
			this.nextVertexFormatIndex();
		}

		if (iris$isTerrain) {
			// ENTITY_ELEMENT
			this.byteBuffer.putShort(0, currentBlock);
			this.byteBuffer.putShort(2, currentRenderType);
			this.nextVertexFormatIndex();
		}
		// MID_TEXTURE_ELEMENT
		byteBuffer.putFloat(0, 0);
		byteBuffer.putFloat(4, 0);
		this.nextVertexFormatIndex();
		// TANGENT_ELEMENT
		this.putInt(0, 0);
		this.nextVertexFormatIndex();
		if (iris$isTerrain) {
			// MID_BLOCK_ELEMENT
			int posIndex = this.nextElementByte - 48;
			float x = byteBuffer.getFloat(posIndex);
			float y = byteBuffer.getFloat(posIndex + 4);
			float z = byteBuffer.getFloat(posIndex + 8);
			this.putInt(0, ExtendedDataHelper.computeMidBlock(x, y, z, currentLocalPosX, currentLocalPosY, currentLocalPosZ));
			this.nextVertexFormatIndex();
		}

		vertexCount++;

		if (drawMode == GL11.GL_QUADS && vertexCount == 4 || drawMode == GL11.GL_TRIANGLES && vertexCount == 3) {
			fillExtendedData(vertexCount);
		}
	}

	@Unique
	private void fillExtendedData(int vertexAmount) {
		vertexCount = 0;

		int stride = vertexFormat.getSize();

		polygon.setup(byteBuffer, nextElementByte, stride, vertexAmount);

		float midU = 0;
		float midV = 0;

		for (int vertex = 0; vertex < vertexAmount; vertex++) {
			midU += polygon.u(vertex);
			midV += polygon.v(vertex);
		}

		midU /= vertexAmount;
		midV /= vertexAmount;

		int midUOffset;
		int midVOffset;
		int normalOffset;
		int tangentOffset;
		if (iris$isTerrain) {
			midUOffset = 16;
			midVOffset = 12;
			normalOffset = 24;
			tangentOffset = 8;
		} else {
			midUOffset = 12;
			midVOffset = 8;
			normalOffset = 16;
			tangentOffset = 4;
		}

		if (vertexAmount == 3) {
			// NormalHelper.computeFaceNormalTri(normal, polygon);	// Removed to enable smooth shaded triangles. Mods rendering triangles with bad normals need to recalculate their normals manually or otherwise shading might be inconsistent.

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				int packedNormal = byteBuffer.getInt(nextElementByte - normalOffset - stride * vertex); // retrieve per-vertex normal

				int tangent = NormalHelper.computeTangentSmooth(NormI8.unpackX(packedNormal), NormI8.unpackY(packedNormal), NormI8.unpackZ(packedNormal), polygon);

				byteBuffer.putFloat(nextElementByte - midUOffset - stride * vertex, midU);
				byteBuffer.putFloat(nextElementByte - midVOffset - stride * vertex, midV);
				byteBuffer.putInt(nextElementByte - tangentOffset - stride * vertex, tangent);
			}
		} else {
			NormalHelper.computeFaceNormal(normal, polygon);
			int packedNormal = NormI8.pack(normal.x, normal.y, normal.z, 0.0f);
			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, polygon);

			for (int vertex = 0; vertex < vertexAmount; vertex++) {
				byteBuffer.putFloat(nextElementByte - midUOffset - stride * vertex, midU);
				byteBuffer.putFloat(nextElementByte - midVOffset - stride * vertex, midV);
				byteBuffer.putInt(nextElementByte - normalOffset - stride * vertex, packedNormal);
				byteBuffer.putInt(nextElementByte - tangentOffset - stride * vertex, tangent);
			}
		}
	}

	@Override
	public void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ) {
		this.currentBlock = block;
		this.currentRenderType = renderType;
		this.currentLocalPosX = localPosX;
		this.currentLocalPosY = localPosY;
		this.currentLocalPosZ = localPosZ;
	}

	@Override
	public void endBlock() {
		this.currentBlock = -1;
		this.currentRenderType = -1;
		this.currentLocalPosX = 0;
		this.currentLocalPosY = 0;
		this.currentLocalPosZ = 0;
	}

	@Unique
	private void putInt(int i, int value) {
		this.byteBuffer.putInt(this.nextElementByte + i, value);
	}
}
