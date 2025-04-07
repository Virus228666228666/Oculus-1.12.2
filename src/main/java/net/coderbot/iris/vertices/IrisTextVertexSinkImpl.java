package net.coderbot.iris.vertices;

import nanolive.compat.CompatMemoryUtil;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.irisshaders.iris.api.v0.IrisTextVertexSink;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

public class IrisTextVertexSinkImpl implements IrisTextVertexSink {
	static VertexFormat format = IrisVertexFormats.TERRAIN;
	private final ByteBuffer buffer;
	private final TextQuadView quad = new TextQuadView();
	private final Vector3f saveNormal = new Vector3f();
	private static final int STRIDE = IrisVertexFormats.TERRAIN.getSize();
	private int vertexCount;
	private long elementOffset;
	private float uSum;
	private float vSum;

	public IrisTextVertexSinkImpl(int maxQuadCount, IntFunction<ByteBuffer> buffer) {
		this.buffer = buffer.apply(format.getSize() * 4 * maxQuadCount);
		//this.elementOffset = MemoryUtil.memAddress(this.buffer);
		this.elementOffset = BufferUtils.getOffset(this.buffer);
	}

	@Override
	public VertexFormat getUnderlyingVertexFormat() {
		return format;
	}

	@Override
	public ByteBuffer getUnderlyingByteBuffer() {
		return buffer;
	}

	@Override
	public void quad(float minX, float minY, float maxX, float maxY, float z, int color, float minU, float minV, float maxU, float maxV, int light) {
		vertex(minX, minY, z, color, minU, minV, light);
		vertex(minX, maxY, z, color, minU, maxV, light);
		vertex(maxX, maxY, z, color, maxU, maxV, light);
		vertex(maxX, minY, z, color, maxU, minV, light);
	}

	private void vertex(float x, float y, float z, int color, float u, float v, int light) {
		vertexCount++;
		uSum += u;
		vSum += v;

		long i = elementOffset;

		CompatMemoryUtil.memPutFloat(i, x);
		CompatMemoryUtil.memPutFloat(i + 4, y);
		CompatMemoryUtil.memPutFloat(i + 8, z);
		CompatMemoryUtil.memPutInt(i + 12, color);
		CompatMemoryUtil.memPutFloat(i + 16, u);
		CompatMemoryUtil.memPutFloat(i + 20, v);
		CompatMemoryUtil.memPutInt(i + 24, light);

		if (vertexCount == 4) {
			// TODO: compute this at the head of quad()
			vertexCount = 0;
			uSum *= 0.25F;
			vSum *= 0.25F;
			quad.setup(elementOffset, IrisVertexFormats.TERRAIN.getSize());

			NormalHelper.computeFaceNormal(saveNormal, quad);
			float normalX = saveNormal.x;
			float normalY = saveNormal.y;
			float normalZ = saveNormal.z;
			int normal = NormalHelper.packNormal(saveNormal, 0.0F);

			int tangent = NormalHelper.computeTangent(normalX, normalY, normalZ, quad);

			for (long vertex = 0; vertex < 4; vertex++) {
				CompatMemoryUtil.memPutFloat(i + 36 - STRIDE * vertex, uSum);
				CompatMemoryUtil.memPutFloat(i + 40 - STRIDE * vertex, vSum);
				CompatMemoryUtil.memPutInt(i + 28 - STRIDE * vertex, normal);
				CompatMemoryUtil.memPutInt(i + 44 - STRIDE * vertex, tangent);
			}

			uSum = 0;
			vSum = 0;
		}

		buffer.position(buffer.position() + STRIDE);
		elementOffset += STRIDE;
	}

	static class TextQuadView implements QuadView {
		long writePointer;
		int stride;

		public TextQuadView() {

		}

		public void setup(long writePointer, int stride) {
			this.writePointer = writePointer;
			this.stride = stride;
		}

		public float x(int index) {
			return CompatMemoryUtil.memGetFloat(writePointer - stride * (3L - index));
		}

		public float y(int index) {
			return CompatMemoryUtil.memGetFloat(writePointer + 4 - stride * (3L - index));
		}

		public float z(int index) {
			return CompatMemoryUtil.memGetFloat(writePointer + 8 - stride * (3L - index));
		}

		public float u(int index) {
			return CompatMemoryUtil.memGetFloat(writePointer + 16 - stride * (3L - index));
		}

		public float v(int index) {
			return CompatMemoryUtil.memGetFloat(writePointer + 20 - stride * (3L - index));
		}
	}
}
