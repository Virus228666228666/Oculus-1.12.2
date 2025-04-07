package net.coderbot.iris.compat.sodium.impl.vertex_format;

import org.lwjgl.opengl.GL11;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import net.coderbot.iris.compat.sodium.mixin.vertex_format.GlVertexAttributeFormatAccessor;

public class IrisGlVertexAttributeFormat {
	public static final GlVertexAttributeFormat BYTE =
			GlVertexAttributeFormatAccessor.createGlVertexAttributeFormat(GL11.GL_BYTE, 1);
	public static final GlVertexAttributeFormat SHORT = GlVertexAttributeFormatAccessor.createGlVertexAttributeFormat(GL11.GL_SHORT, 2);
}
