package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import java.nio.FloatBuffer;

import me.jellysquid.mods.sodium.client.render.GameRendererContext;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.ResourceLocation;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderFogComponent;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;

import javax.annotation.Nullable;

public class IrisChunkProgram extends ChunkProgram {
	// Uniform variable binding indexes
	private final int uModelViewMatrix;
	private final int uNormalMatrix;

	@Nullable
	private final ProgramUniforms irisProgramUniforms;

	@Nullable
	private final ProgramSamplers irisProgramSamplers;

	@Nullable
	private final ProgramImages irisProgramImages;

	public IrisChunkProgram(RenderDevice owner, ResourceLocation name, int handle,
							@Nullable ProgramUniforms irisProgramUniforms, @Nullable ProgramSamplers irisProgramSamplers,
							@Nullable ProgramImages irisProgramImages) {
		super(owner, name, handle, ChunkShaderFogComponent.None::new);
		this.uModelViewMatrix = this.getUniformLocation("iris_ModelViewMatrix");
		this.uNormalMatrix = this.getUniformLocation("iris_NormalMatrix");
		this.irisProgramUniforms = irisProgramUniforms;
		this.irisProgramSamplers = irisProgramSamplers;
		this.irisProgramImages = irisProgramImages;
	}

	@Override
	public void setup(float modelScale, float textureScale) {
		super.setup(modelScale, textureScale);

		if (irisProgramUniforms != null) {
			irisProgramUniforms.update();
		}

		if (irisProgramSamplers != null) {
			irisProgramSamplers.update();
		}

		if (irisProgramImages != null) {
			irisProgramImages.update();
		}

		FloatBuffer normalMatrixBuffer = FloatBuffer.allocate(GameRendererContext.getModelViewProjectionMatrix().capacity());
		float[] normalMatrix = new float[16];
		normalMatrixBuffer.get(normalMatrix);
		float[] invertedMatrix = new float[16];
		invertMatrix(normalMatrix, invertedMatrix);
		transposeMatrix(invertedMatrix);
		normalMatrixBuffer.put(invertedMatrix);
		normalMatrixBuffer.flip();

		uniformMatrix(uModelViewMatrix, GameRendererContext.getModelViewProjectionMatrix());
		uniformMatrix(uNormalMatrix, normalMatrixBuffer);
	}

	@Override
	public int getUniformLocation(String name) {
		// NB: We pass through calls involving u_ModelViewProjectionMatrix, u_ModelScale, and u_TextureScale, since
		//     currently patched Iris shader programs use those.

		if ("iris_BlockTex".equals(name) || "iris_LightTex".equals(name)) {
			// Not relevant for Iris shader programs
			return -1;
		}

		try {
			return super.getUniformLocation(name);
		} catch (NullPointerException e) {
			// Suppress getUniformLocation
			return -1;
		}
	}

	private void uniformMatrix(int location, FloatBuffer buffer) {
		if (location == -1) {
			return;
		}

		IrisRenderSystem.uniformMatrix4fv(location, false, buffer);
	}

	private static boolean invertMatrix(float[] m, float[] invOut) {
		float[] inv = new float[16];

		inv[0] = m[5]  * m[10] * m[15] -
				m[5]  * m[11] * m[14] -
				m[9]  * m[6]  * m[15] +
				m[9]  * m[7]  * m[14] +
				m[13] * m[6]  * m[11] -
				m[13] * m[7]  * m[10];

		inv[4] = -m[4]  * m[10] * m[15] +
				m[4]  * m[11] * m[14] +
				m[8]  * m[6]  * m[15] -
				m[8]  * m[7]  * m[14] -
				m[12] * m[6]  * m[11] +
				m[12] * m[7]  * m[10];

		inv[8] = m[4]  * m[9] * m[15] -
				m[4]  * m[11] * m[13] -
				m[8]  * m[5] * m[15] +
				m[8]  * m[7] * m[13] +
				m[12] * m[5] * m[11] -
				m[12] * m[7] * m[9];

		inv[12] = -m[4]  * m[9] * m[14] +
				m[4]  * m[10] * m[13] +
				m[8]  * m[5] * m[14] -
				m[8]  * m[6] * m[13] -
				m[12] * m[5] * m[10] +
				m[12] * m[6] * m[9];

		inv[1] = -m[1]  * m[10] * m[15] +
				m[1]  * m[11] * m[14] +
				m[9]  * m[2] * m[15] -
				m[9]  * m[3] * m[14] -
				m[13] * m[2] * m[11] +
				m[13] * m[3] * m[10];

		inv[5] = m[0]  * m[10] * m[15] -
				m[0]  * m[11] * m[14] -
				m[8]  * m[2] * m[15] +
				m[8]  * m[3] * m[14] +
				m[12] * m[2] * m[11] -
				m[12] * m[3] * m[10];

		inv[9] = -m[0]  * m[9] * m[15] +
				m[0]  * m[11] * m[13] +
				m[8]  * m[1] * m[15] -
				m[8]  * m[3] * m[13] -
				m[12] * m[1] * m[11] +
				m[12] * m[3] * m[9];

		inv[13] = m[0]  * m[9] * m[14] -
				m[0]  * m[10] * m[13] -
				m[8]  * m[1] * m[14] +
				m[8]  * m[2] * m[13] +
				m[12] * m[1] * m[10] -
				m[12] * m[2] * m[9];

		inv[2] = m[1]  * m[6] * m[15] -
				m[1]  * m[7] * m[14] -
				m[5]  * m[2] * m[15] +
				m[5]  * m[3] * m[14] +
				m[13] * m[2] * m[7] -
				m[13] * m[3] * m[6];

		inv[6] = -m[0]  * m[6] * m[15] +
				m[0]  * m[7] * m[14] +
				m[4]  * m[2] * m[15] -
				m[4]  * m[3] * m[14] -
				m[12] * m[2] * m[7] +
				m[12] * m[3] * m[6];

		inv[10] = m[0]  * m[5] * m[15] -
				m[0]  * m[7] * m[13] -
				m[4]  * m[1] * m[15] +
				m[4]  * m[3] * m[13] +
				m[12] * m[1] * m[7] -
				m[12] * m[3] * m[5];

		inv[14] = -m[0]  * m[5] * m[14] +
				m[0]  * m[6] * m[13] +
				m[4]  * m[1] * m[14] -
				m[4]  * m[2] * m[13] -
				m[12] * m[1] * m[6] +
				m[12] * m[2] * m[5];

		inv[3] = -m[1] * m[6] * m[11] +
				m[1] * m[7] * m[10] +
				m[5] * m[2] * m[11] -
				m[5] * m[3] * m[10] -
				m[9] * m[2] * m[7] +
				m[9] * m[3] * m[6];

		inv[7] = m[0] * m[6] * m[11] -
				m[0] * m[7] * m[10] -
				m[4] * m[2] * m[11] +
				m[4] * m[3] * m[10] +
				m[8] * m[2] * m[7] -
				m[8] * m[3] * m[6];

		inv[11] = -m[0] * m[5] * m[11] +
				m[0] * m[7] * m[9] +
				m[4] * m[1] * m[11] -
				m[4] * m[3] * m[9] -
				m[8] * m[1] * m[7] +
				m[8] * m[3] * m[5];

		inv[15] = m[0] * m[5] * m[10] -
				m[0] * m[6] * m[9] -
				m[4] * m[1] * m[10] +
				m[4] * m[2] * m[9] +
				m[8] * m[1] * m[6] -
				m[8] * m[2] * m[5];

		float det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];

		if (det == 0)
			return false;

		det = 1.0f / det;

		for (int i = 0; i < 16; i++)
			invOut[i] = inv[i] * det;

		return true;
	}

	private static void transposeMatrix(float[] m) {
		float temp;
		for (int i = 0; i < 4; i++) {
			for (int j = i + 1; j < 4; j++) {
				temp = m[i * 4 + j];
				m[i * 4 + j] = m[j * 4 + i];
				m[j * 4 + i] = temp;
			}
		}
	}


}
