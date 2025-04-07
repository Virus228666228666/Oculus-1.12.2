package net.coderbot.batchedentityrendering.impl;

import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL11;

public class RenderLayerUtil {

    public static boolean isTriangleStripDrawMode(BlockRenderLayer renderLayer) {
        return getGLMode(renderLayer) == GL11.GL_TRIANGLE_STRIP;
    }

    public static int getGLMode(BlockRenderLayer layer) {
        // todo: double-check
        switch (layer) {
            case SOLID:
                return GL11.GL_QUADS;
            case CUTOUT_MIPPED:
                return GL11.GL_QUADS;
            case CUTOUT:
                return GL11.GL_QUADS;
            case TRANSLUCENT:
                return GL11.GL_TRIANGLES;
            default:
                return GL11.GL_QUADS;
        }
    }
}
