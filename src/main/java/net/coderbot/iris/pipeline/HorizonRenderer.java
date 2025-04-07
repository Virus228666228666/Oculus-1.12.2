package net.coderbot.iris.pipeline;

import nanolive.compat.VertexUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Renders the sky horizon. Vanilla Minecraft simply uses the "clear color" for its horizon, and then draws a plane
 * above the player. This class extends the sky rendering so that an octagonal prism is drawn around the player instead,
 * allowing shaders to perform more advanced sky rendering.
 * <p>
 * However, the horizon rendering is designed so that when sky shaders are not being used, it looks almost exactly the
 * same as vanilla sky rendering, except a few almost entirely imperceptible differences where the walls
 * of the octagonal prism intersect the top plane.
 */
public class HorizonRenderer {
	/**
	 * The Y coordinate of the top skybox plane. Acts as the upper bound for the horizon prism, since the prism lies
	 * between the bottom and top skybox planes.
	 */
	private static final float TOP = 16.0F;

	/**
	 * The Y coordinate of the bottom skybox plane. Acts as the lower bound for the horizon prism, since the prism lies
	 * between the bottom and top skybox planes.
	 */
	private static final float BOTTOM = -16.0F;

	/**
	 * Cosine of 22.5 degrees.
	 */
	private static final double COS_22_5 = Math.cos(Math.toRadians(22.5));

	/**
	 * Sine of 22.5 degrees.
	 */
	private static final double SIN_22_5 = Math.sin(Math.toRadians(22.5));
	private VertexBuffer buffer;
	private int currentRenderDistance;
	private int bufferCount;

	public HorizonRenderer() {
		currentRenderDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;

		rebuildBuffer();
	}

	private void rebuildBuffer() {
		if (this.buffer != null) {
			this.buffer.deleteGlBuffers();
		}

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		// Build the horizon quads into a buffer
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		buildHorizon(currentRenderDistance * 16, buffer);
		buffer.finishDrawing();

		this.buffer = new VertexBuffer(DefaultVertexFormats.POSITION);
		this.buffer.bindBuffer();
		// replacement for this.buffer.upload()
		ByteBuffer lvt_2_1_ = buffer.getByteBuffer();
		if (OpenGlHelper.glGenBuffers() != -1) {
			this.bufferCount = lvt_2_1_.remaining() / DefaultVertexFormats.POSITION.getSize();
			this.buffer.bindBuffer();
			GL15.glBufferData(34962, lvt_2_1_, 35044);
			this.buffer.unbindBuffer();
		}
		this.buffer.unbindBuffer();
	}

	private void buildQuad(BufferBuilder consumer, double x1, double z1, double x2, double z2) {
		consumer.pos(x1, BOTTOM, z1);
		consumer.endVertex();
		consumer.pos(x1, TOP, z1);
		consumer.endVertex();
		consumer.pos(x2, TOP, z2);
		consumer.endVertex();
		consumer.pos(x2, BOTTOM, z2);
		consumer.endVertex();
	}

	private void buildHalf(BufferBuilder consumer, double adjacent, double opposite, boolean invert) {
		if (invert) {
			adjacent = -adjacent;
			opposite = -opposite;
		}

		// NB: Make sure that these vertices are being specified in counterclockwise order!
		// Otherwise back face culling will remove your quads, and you'll be wondering why there's a hole in your horizon.
		// Don't poke holes in the horizon. Specify vertices in counterclockwise order.

		// +X,-Z face
		buildQuad(consumer, adjacent, -opposite, opposite, -adjacent);
		// +X face
		buildQuad(consumer, adjacent, opposite, adjacent, -opposite);
		// +X,+Z face
		buildQuad(consumer, opposite, adjacent, adjacent, opposite);
		// +Z face
		buildQuad(consumer, -opposite, adjacent, opposite, adjacent);
	}

	/**
	 * @param adjacent the adjacent side length of the a triangle with a hypotenuse extending from the center of the
	 *                 octagon to a given vertex on the perimeter.
	 * @param opposite the opposite side length of the a triangle with a hypotenuse extending from the center of the
	 *                 octagon to a given vertex on the perimeter.
	 */
	private void buildOctagonalPrism(BufferBuilder consumer, double adjacent, double opposite) {
		buildHalf(consumer, adjacent, opposite, false);
		buildHalf(consumer, adjacent, opposite, true);
	}

	private void buildRegularOctagonalPrism(BufferBuilder consumer, double radius) {
		buildOctagonalPrism(consumer, radius * COS_22_5, radius * SIN_22_5);
	}

	private void buildBottomPlane(BufferBuilder consumer, int radius) {
		for (int x = -radius; x <= radius; x += 64) {
			for (int z = -radius; z <= radius; z += 64) {
				consumer.pos(x + 64, BOTTOM, z);
				consumer.endVertex();
				consumer.pos(x, BOTTOM, z);
				consumer.endVertex();
				consumer.pos(x, BOTTOM, z + 64);
				consumer.endVertex();
				consumer.pos(x + 64, BOTTOM, z + 64);
				consumer.endVertex();
			}
		}
	}

	private void buildTopPlane(BufferBuilder consumer, int radius) {
		// You might be tempted to try to combine this with buildBottomPlane to avoid code duplication,
		// but that won't work since the winding order has to be reversed or else one of the planes will be
		// discarded by back face culling.
		for (int x = -radius; x <= radius; x += 64) {
			for (int z = -radius; z <= radius; z += 64) {
				consumer.pos(x + 64, TOP, z);
				consumer.endVertex();
				consumer.pos(x + 64, TOP, z + 64);
				consumer.endVertex();
				consumer.pos(x, TOP, z + 64);
				consumer.endVertex();
				consumer.pos(x, TOP, z);
				consumer.endVertex();
			}
		}
	}

	private void buildHorizon(int radius, BufferBuilder consumer) {
		if (radius > 256) {
			// Prevent the prism from getting too large, this causes issues on some shader packs that modify the vanilla
			// sky if we don't do this.
			radius = 256;
		}

		buildRegularOctagonalPrism(consumer, radius);

		// Replicate the vanilla top plane since we can't assume that it'll be rendered.
		// TODO: Remove vanilla top plane
		buildTopPlane(consumer, 384);

		// Always make the bottom plane have a radius of 384, to match the top plane.
		buildBottomPlane(consumer, 384);
	}

	public void renderHorizon(Matrix4f matrix) {
		if (currentRenderDistance != Minecraft.getMinecraft().gameSettings.renderDistanceChunks) {
			currentRenderDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
			rebuildBuffer();
		}

		buffer.bindBuffer();
		VertexUtils.setupBufferState(DefaultVertexFormats.POSITION, 0L);

		// replacement for buffer.draw(matrix, GL11.GL_QUADS);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
		if (matrix != null) {
			floatBuffer.put(0, matrix.m00)
					.put(1, matrix.m01)
					.put(2, matrix.m02)
					.put(3, matrix.m03)
					.put(4, matrix.m10)
					.put(5, matrix.m11)
					.put(6, matrix.m12)
					.put(7, matrix.m13)
					.put(8, matrix.m20)
					.put(9, matrix.m21)
					.put(10, matrix.m22)
					.put(11, matrix.m23)
					.put(12, matrix.m30)
					.put(13, matrix.m31)
					.put(14, matrix.m32)
					.put(15, matrix.m33);
			floatBuffer.rewind();
			GL11.glMultMatrix(floatBuffer);
		}
		GL11.glDrawArrays(GL11.GL_QUADS, 0, bufferCount);
		GlStateManager.popMatrix();

		VertexUtils.clearBufferState(DefaultVertexFormats.POSITION);
		this.buffer.unbindBuffer();
	}

	public void destroy() {
		buffer.deleteGlBuffers();
	}
}
