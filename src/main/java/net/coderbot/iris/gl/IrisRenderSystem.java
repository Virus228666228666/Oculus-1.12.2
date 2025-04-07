package net.coderbot.iris.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;

import net.coderbot.iris.Iris;
import net.coderbot.iris.vendored.joml.Vector3i;

/**
 * This class is responsible for abstracting calls to OpenGL and asserting that calls are run on the render thread.
 */
public class IrisRenderSystem {
	private static DSAAccess dsaState;
	private static boolean hasMultibind;
	private static boolean supportsCompute;

	public static void initRenderer() {
		if (GLContext.getCapabilities().OpenGL45) {
			dsaState = new DSACore();
			Iris.logger.info("OpenGL 4.5 detected, enabling DSA.");
		} else if (GLContext.getCapabilities().GL_ARB_direct_state_access) {
			dsaState = new DSAARB();
			Iris.logger.info("ARB_direct_state_access detected, enabling DSA.");
		} else {
			dsaState = new DSAUnsupported();
			Iris.logger.info("DSA support not detected.");
		}

		if (GLContext.getCapabilities().OpenGL45 || GLContext.getCapabilities().GL_ARB_multi_bind) {
			hasMultibind = true;
		} else {
			hasMultibind = false;
		}

		supportsCompute = supportsCompute();
	}

	public static void getIntegerv(int pname, int[] params) {
		if (params.length == 1) {
			params[0] = GL11.glGetInteger(pname);
		} else {
			IntBuffer buffer = BufferUtils.createIntBuffer(params.length);
			GL11.glGetInteger(pname, buffer);
			buffer.get(params);
		}
	}

	public static void getFloatv(int pname, float[] params) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(params.length);
		GL11.glGetFloat(pname, buffer);
		buffer.get(params);
	}

	public static void generateMipmaps(int texture, int mipmapTarget) {
		dsaState.generateMipmaps(texture, mipmapTarget);
	}

	public static void bindAttributeLocation(int program, int index, CharSequence name) {
		GL20.glBindAttribLocation(program, index, name);
	}

	public static void texImage2D(int texture, int target, int level, int internalFormat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
		GlStateManager.bindTexture(texture);
		GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}

	public static void uniformMatrix4fv(int location, boolean transpose, FloatBuffer matrix) {
		GL20.glUniformMatrix4(location, transpose, matrix);
	}

	public static void copyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
		GL11.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
	}

	public static void uniform1f(int location, float v0) {
		GL20.glUniform1f(location, v0);
	}

	public static void uniform1i(int location, int v0) {
		GL20.glUniform1i(location, v0);
	}

	public static void uniform2f(int location, float v0, float v1) {
		GL20.glUniform2f(location, v0, v1);
	}

	public static void uniform2i(int location, int v0, int v1) {
		GL20.glUniform2i(location, v0, v1);
	}

	public static void uniform3f(int location, float v0, float v1, float v2) {
		GL20.glUniform3f(location, v0, v1, v2);
	}

	public static void uniform4f(int location, float v0, float v1, float v2, float v3) {
		GL20.glUniform4f(location, v0, v1, v2, v3);
	}

	public static void uniform4i(int location, int v0, int v1, int v2, int v3) {
		GL20.glUniform4i(location, v0, v1, v2, v3);
	}

	public static int getAttribLocation(int programId, String name) {
		return GL20.glGetAttribLocation(programId, name);
	}

	public static int getUniformLocation(int programId, String name) {
		return GL20.glGetUniformLocation(programId, name);
	}

	public static void texParameteriv(int texture, int target, int pname, int[] params) {
		GlStateManager.bindTexture(texture);
		IntBuffer buffer = BufferUtils.createIntBuffer(params.length).put(params);
		buffer.flip();
		GL11.glTexParameter(target, pname, buffer);
	}

	public static void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
		dsaState.copyTexSubImage2D(destTexture, target, i, i1, i2, i3, i4, width, height);
	}

	public static void texParameteri(int texture, int target, int pname, int param) {
		dsaState.texParameteri(texture, target, pname, param);
	}

	public static void texParameterf(int texture, int target, int pname, float param) {
		dsaState.texParameterf(texture, target, pname, param);
	}

	public static String getProgramInfoLog(int program) {
		return GL20.glGetProgramInfoLog(program, 2048); // arbitrary maxLength
	}

	public static String getShaderInfoLog(int shader) {
		return GL20.glGetShaderInfoLog(shader, 2048);
	}

	public static void drawBuffers(int framebuffer, int[] buffers) {
		dsaState.drawBuffers(framebuffer, buffers);
	}

	public static void readBuffer(int framebuffer, int buffer) {
		dsaState.readBuffer(framebuffer, buffer);
	}

	public static String getActiveUniform(int program, int index, int maxLength, IntBuffer size, IntBuffer type) {
		IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
		ByteBuffer nameBuffer = BufferUtils.createByteBuffer(maxLength);

		GL20.glGetActiveUniform(program, index, lengthBuffer, size, type, nameBuffer);

		int length = lengthBuffer.get(0);
		byte[] nameBytes = new byte[length];
		nameBuffer.get(nameBytes);
		return new String(nameBytes);
	}

	public static void readPixels(int x, int y, int width, int height, int format, int type, float[] pixels) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(pixels.length);
		GL11.glReadPixels(x, y, width, height, format, type, buffer);
		buffer.get(pixels);
	}

	public static void bufferData(int target, float[] data, int usage) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length).put(data);
		buffer.flip();
		GL15.glBufferData(target, buffer, usage);
	}

	public static int bufferStorage(int target, float[] data, int usage) {
		return dsaState.bufferStorage(target, data, usage);
	}

	public static void vertexAttrib4f(int index, float v0, float v1, float v2, float v3) {
		GL20.glVertexAttrib4f(index, v0, v1, v2, v3);
	}

	public static void detachShader(int program, int shader) {
		GL20.glDetachShader(program, shader);
	}

	public static void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
		dsaState.framebufferTexture2D(fb, fbtarget, attachment, target, texture, levels);
	}

	public static int getTexParameteri(int texture, int target, int pname) {
		return dsaState.getTexParameteri(texture, target, pname);
	}

	public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		if (GLContext.getCapabilities().OpenGL42) {
			GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
		} else {
			EXTShaderImageLoadStore.glBindImageTextureEXT(unit, texture, level, layered, layer, access, format);
		}
	}

	public static int getMaxImageUnits() {
		if (GLContext.getCapabilities().OpenGL42) {
			return GL11.glGetInteger(GL42.GL_MAX_IMAGE_UNITS);
		} else if (GLContext.getCapabilities().GL_EXT_shader_image_load_store) {
			return GL11.glGetInteger(EXTShaderImageLoadStore.GL_MAX_IMAGE_UNITS_EXT);
		} else {
			return 0;
		}
	}

	public static void getProgramiv(int program, int value, int[] storage) {
		IntBuffer buffer = BufferUtils.createIntBuffer(storage.length);
		GL20.glGetProgram(program, value, buffer);
		buffer.rewind();
		buffer.get(storage);
	}

	public static void dispatchCompute(int workX, int workY, int workZ) {
		GL43.glDispatchCompute(workX, workY, workZ);
	}

	public static void dispatchCompute(Vector3i workGroups) {
		GL43.glDispatchCompute(workGroups.x, workGroups.y, workGroups.z);
	}

	public static void memoryBarrier(int barriers) {
		if (supportsCompute) {
			GL42.glMemoryBarrier(barriers);
		}
	}

	public static boolean supportsBufferBlending() {
		return GLContext.getCapabilities().GL_ARB_draw_buffers_blend || GLContext.getCapabilities().OpenGL40;
	}

	public static void disableBufferBlend(int buffer) {
		EXTDrawBuffers2.glDisableIndexedEXT(GL11.GL_BLEND, buffer);
	}

	public static void enableBufferBlend(int buffer) {
		EXTDrawBuffers2.glEnableIndexedEXT(GL11.GL_BLEND, buffer);
	}

	public static void blendFuncSeparatei(int buffer, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL40.glBlendFuncSeparatei(buffer, srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public static void bindTextureToUnit(int unit, int texture) {
		dsaState.bindTextureToUnit(unit, texture);
	}

	// These functions are deprecated and unavailable in the core profile.

	@Deprecated
	public static void setupProjectionMatrix(float[] matrix) {
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		FloatBuffer buffer = BufferUtils.createFloatBuffer(matrix.length);
		buffer.put(matrix);
		buffer.flip();
		GL11.glLoadMatrix(buffer);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
	}

	@Deprecated
	public static void restoreProjectionMatrix() {
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
	}

	public static void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
		dsaState.blitFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
	}

	public static int createFramebuffer() {
		return dsaState.createFramebuffer();
	}

	public static int createTexture(int target) {
		return dsaState.createTexture(target);
	}

	public interface DSAAccess {
		void generateMipmaps(int texture, int target);

		void texParameteri(int texture, int target, int pname, int param);
		void texParameterf(int texture, int target, int pname, float param);
		void texParameteriv(int texture, int target, int pname, int[] params);

		void readBuffer(int framebuffer, int buffer);

		void drawBuffers(int framebuffer, int[] buffers);

		int getTexParameteri(int texture, int target, int pname);

		void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height);

		void bindTextureToUnit(int unit, int texture);

		int bufferStorage(int target, float[] data, int usage);

		void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter);

		void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels);

		int createFramebuffer();
		int createTexture(int target);
	}

	public static class DSACore extends DSAARB {

	}

	public static class DSAARB extends DSAUnsupported {

		@Override
		public void generateMipmaps(int texture, int target) {
			ARBDirectStateAccess.glGenerateTextureMipmap(texture);
		}

		@Override
		public void texParameteri(int texture, int target, int pname, int param) {
			ARBDirectStateAccess.glTextureParameteri(texture, pname, param);
		}

		@Override
		public void texParameterf(int texture, int target, int pname, float param) {
			ARBDirectStateAccess.glTextureParameterf(texture, pname, param);
		}

//		@Override
//		public void texParameteriv(int texture, int target, int pname, int[] params) {
//			GL45.glTextureParameteriv(texture, pname, params);
//		}

		@Override
		public void readBuffer(int framebuffer, int buffer) {
			ARBDirectStateAccess.glNamedFramebufferReadBuffer(framebuffer, buffer);
		}

		@Override
		public void drawBuffers(int framebuffer, int[] buffers) {
			IntBuffer buffer = BufferUtils.createIntBuffer(buffers.length);
			ARBDirectStateAccess.glNamedFramebufferDrawBuffers(framebuffer, buffer);
			buffer.rewind();
			buffer.get(buffers);
		}

		@Override
		public int getTexParameteri(int texture, int target, int pname) {
			return ARBDirectStateAccess.glGetTextureParameteri(texture, pname);
		}

		@Override
		public void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
			ARBDirectStateAccess.glCopyTextureSubImage2D(destTexture, i, i1, i2, i3, i4, width, height);
		}

		@Override
		public void bindTextureToUnit(int unit, int texture) {
			if (texture == 0) {
				super.bindTextureToUnit(unit, texture);
			} else {
				ARBDirectStateAccess.glBindTextureUnit(unit, texture);

				// Manually fix GLStateManager bindings...
				// GlStateManagerAccessor.getTEXTURES()[unit].binding = texture;
			}
		}

		@Override
		public int bufferStorage(int target, float[] data, int usage) {
			int buffer = GL45.glCreateBuffers();
			FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(data.length);
			floatBuffer.put(data);
			floatBuffer.flip();
			GL45.glNamedBufferData(buffer, floatBuffer, usage);
			return buffer;
		}

		@Override
		public void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
			ARBDirectStateAccess.glBlitNamedFramebuffer(source, dest, offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
		}

		@Override
		public void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
			ARBDirectStateAccess.glNamedFramebufferTexture(fb, attachment, texture, levels);
		}

		@Override
		public int createFramebuffer() {
			return ARBDirectStateAccess.glCreateFramebuffers();
		}

		@Override
		public int createTexture(int target) {
			return ARBDirectStateAccess.glCreateTextures(target);
		}
	}

	public static class DSAUnsupported implements DSAAccess {
		@Override
		public void generateMipmaps(int texture, int target) {
			GlStateManager.bindTexture(texture);
			GL30.glGenerateMipmap(target);
		}

		@Override
		public void texParameteri(int texture, int target, int pname, int param) {
			GlStateManager.bindTexture(texture);
			GL11.glTexParameteri(target, pname, param);
		}

		@Override
		public void texParameterf(int texture, int target, int pname, float param) {
			GlStateManager.bindTexture(texture);
			GL11.glTexParameterf(target, pname, param);
		}

		@Override
		public void texParameteriv(int texture, int target, int pname, int[] params) {
			GlStateManager.bindTexture(texture);
			IntBuffer buffer = BufferUtils.createIntBuffer(params.length);
			buffer.put(params);
			buffer.flip();
			GL11.glTexParameter(target, pname, buffer);
		}

		@Override
		public void readBuffer(int framebuffer, int buffer) {
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebuffer);
			GL11.glReadBuffer(buffer);
		}

		@Override
		public void drawBuffers(int framebuffer, int[] buffers) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
			IntBuffer buffer = BufferUtils.createIntBuffer(buffers.length);
			buffer.put(buffers);
			buffer.flip();
			GL20.glDrawBuffers(buffer);
		}

		@Override
		public int getTexParameteri(int texture, int target, int pname) {
			GlStateManager.bindTexture(texture);
			return GL11.glGetTexParameteri(target, pname);
		}

		@Override
		public void copyTexSubImage2D(int destTexture, int target, int i, int i1, int i2, int i3, int i4, int width, int height) {
			GlStateManager.bindTexture(destTexture);
			GL11.glCopyTexSubImage2D(target, i, i1, i2, i3, i4, width, height);
		}

		@Override
		public void bindTextureToUnit(int unit, int texture) {
			GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + unit);
			GlStateManager.bindTexture(texture);
		}

		@Override
		public int bufferStorage(int target, float[] data, int usage) {
			int buffer = GL15.glGenBuffers();
			GL15.glBindBuffer(target, buffer);
			bufferData(target, data, usage);
			GL15.glBindBuffer(target, 0);
			return buffer;
		}

		@Override
		public void blitFramebuffer(int source, int dest, int offsetX, int offsetY, int width, int height, int offsetX2, int offsetY2, int width2, int height2, int bufferChoice, int filter) {
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dest);
			GL30.glBlitFramebuffer(offsetX, offsetY, width, height, offsetX2, offsetY2, width2, height2, bufferChoice, filter);
		}

		@Override
		public void framebufferTexture2D(int fb, int fbtarget, int attachment, int target, int texture, int levels) {
			GL30.glBindFramebuffer(fbtarget, fb);
			GL30.glFramebufferTexture2D(fbtarget, attachment, target, texture, levels);
		}

		@Override
		public int createFramebuffer() {
			int framebuffer = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
			return framebuffer;
		}

		@Override
		public int createTexture(int target) {
			int texture = GlStateManager.generateTexture();
			GlStateManager.bindTexture(texture);
			return texture;
		}
	}

	/*
	public static void bindTextures(int startingTexture, int[] bindings) {
		if (hasMultibind) {
			ARBMultiBind.glBindTextures(startingTexture, bindings);
		} else if (dsaState != DSAState.NONE) {
			for (int binding : bindings) {
				ARBDirectStateAccess.glBindTextureUnit(startingTexture, binding);
				startingTexture++;
			}
		} else {
			for (int binding : bindings) {
				GlStateManager._activeTexture(startingTexture);
				GlStateManager._bindTexture(binding);
				startingTexture++;
			}
		}
	}
	 */

	// TODO: Proper notification of compute support
	public static boolean supportsCompute() {
		return GLContext.getCapabilities().GL_ARB_compute_shader && GLContext.getCapabilities().GL_ARB_shader_storage_buffer_object;
	}
}
