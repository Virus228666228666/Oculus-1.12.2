// Copyright 2020 Grondag
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package net.coderbot.iris.gl.shader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
//import org.lwjgl.opengl.GL20C;
//import org.lwjgl.system.MemoryStack;
//import org.lwjgl.system.MemoryUtil;

/**
 * Contains a workaround for a crash in nglShaderSource on some AMD drivers. Copied from the following Canvas commit:
 * <a href="https://github.com/grondag/canvas/commit/820bf754092ccaf8d0c169620c2ff575722d7d96">...</a>
 * Modified for 1.12.2
 */
public class ShaderWorkarounds {
	/**
	 * Identical in function to @link GL20C#glShaderSource(int, CharSequence) but
	 * passes a null pointer for string length to force the driver to rely on the null
	 * terminator for string length.  This is a workaround for an apparent flaw with some
	 * AMD drivers that don't receive or interpret the length correctly, resulting in
	 * an access violation when the driver tries to read past the string memory.
	 *
	 * <p>Hat tip to fewizz for the find and the fix.
	 */
	public static void safeShaderSource(int glId, CharSequence source) {
		ByteBuffer sourceBuffer = BufferUtils.createByteBuffer(source.length() + 1);
		sourceBuffer.put(source.toString().getBytes());
		sourceBuffer.put((byte) 0);  // Null-terminate the string
		sourceBuffer.flip();

		IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
		lengthBuffer.put(source.length());
		lengthBuffer.flip();

		GL20.glShaderSource(glId, sourceBuffer);
	}
}
