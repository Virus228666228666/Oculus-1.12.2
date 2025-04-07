package net.coderbot.iris.mixin;

import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
	@Accessor("activeTextureUnit")
	static int getActiveTexture() {
		throw new AssertionError();
	}

	@Accessor("alphaState")
	static GlStateManager.AlphaState getALPHA_TEST() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("blendState")
	static GlStateManager.BlendState getBLEND() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("colorMaskState")
	static GlStateManager.ColorMask getCOLOR_MASK() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("depthState")
	static GlStateManager.DepthState getDEPTH() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("fogState")
	static GlStateManager.FogState getFOG() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("textureState")
	static GlStateManager.TextureState[] getTEXTURES() {
		throw new UnsupportedOperationException("Not accessed");
	}
}
