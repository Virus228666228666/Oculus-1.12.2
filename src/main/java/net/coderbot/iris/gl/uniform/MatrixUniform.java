package net.coderbot.iris.gl.uniform;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import net.minecraft.client.renderer.Matrix4f;
import org.lwjgl.BufferUtils;

import net.coderbot.iris.gl.IrisRenderSystem;

public class MatrixUniform extends Uniform {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	MatrixUniform(int location, Supplier<Matrix4f> value) {
		super(location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public void update() {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = newValue;

			cachedValue.store(buffer);
			buffer.rewind();

			IrisRenderSystem.uniformMatrix4fv(location, false, buffer);
		}
	}
}
