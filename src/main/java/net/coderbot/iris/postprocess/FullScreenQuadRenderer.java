package net.coderbot.iris.postprocess;

import nanolive.compat.VertexUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import net.coderbot.iris.gl.IrisRenderSystem;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	private final int quadBuffer;

	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private FullScreenQuadRenderer() {
		this.quadBuffer = createQuad();
	}

	public void render() {
		begin();

		renderQuad();

		end();
	}

	@SuppressWarnings("deprecation")
	public void begin() {
		GlStateManager.disableDepth();

		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		// scale the quad from [0, 1] to [-1, 1]
		GlStateManager.translate(-1.0F, -1.0F, 0.0F);
		GlStateManager.scale(2.0F, 2.0F, 0.0F);

		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadBuffer);
		VertexUtils.setupBufferState(DefaultVertexFormats.POSITION_TEX, 0L);
	}

	public void renderQuad() {
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
	}

	@SuppressWarnings("deprecation")
	public static void end() {
		VertexUtils.clearBufferState(DefaultVertexFormats.POSITION_TEX);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GlStateManager.enableDepth();

		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
	}

	/**
	 * Creates and uploads a vertex buffer containing a single full-screen quad
	 */
	private static int createQuad() {
		float[] vertices = new float[] {
				// Vertex 0: Top right corner
				1.0F, 1.0F, 0.0F,
				1.0F, 1.0F,
				// Vertex 1: Top left corner
				0.0F, 1.0F, 0.0F,
				0.0F, 1.0F,
				// Vertex 2: Bottom right corner
				1.0F, 0.0F, 0.0F,
				1.0F, 0.0F,
				// Vertex 3: Bottom left corner
				0.0F, 0.0F, 0.0F,
				0.0F, 0.0F
		};

		return IrisRenderSystem.bufferStorage(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
	}
}
