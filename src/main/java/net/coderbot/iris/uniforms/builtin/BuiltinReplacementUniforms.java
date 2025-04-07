package net.coderbot.iris.uniforms.builtin;

import net.minecraft.client.renderer.Matrix4f;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;

public class BuiltinReplacementUniforms {
	private static final Matrix4f lightmapTextureMatrix;

	static {
		// This mimics the transformations done in LightmapTextureManager to the GL_TEXTURE matrix.
		lightmapTextureMatrix = new Matrix4f();
		lightmapTextureMatrix.setIdentity();
		float[] valueArray = new float[16];
		Arrays.fill(valueArray, 0.00390625f);
		Matrix4f tempMatrix = new Matrix4f(valueArray);
		Matrix4f.mul(lightmapTextureMatrix, tempMatrix, lightmapTextureMatrix);
		lightmapTextureMatrix.translate(new Vector3f(8.0f, 8.0f, 8.0f));
	}

	public static void addBuiltinReplacementUniforms(UniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.ONCE, "iris_LightmapTextureMatrix", () -> {
			Iris.logger.warn("A shader appears to require the lightmap texture matrix even after transformations have occurred");
			Iris.logger.warn("Iris handles this correctly but it indicates that the shader is doing weird things with lightmap coordinates");

			return lightmapTextureMatrix;
		});
	}
}
