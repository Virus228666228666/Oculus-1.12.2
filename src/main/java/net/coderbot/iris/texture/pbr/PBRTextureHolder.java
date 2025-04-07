package net.coderbot.iris.texture.pbr;

import net.minecraft.client.renderer.texture.AbstractTexture;

import javax.annotation.Nonnull;

public interface PBRTextureHolder {
	@Nonnull
	AbstractTexture getNormalTexture();

	@Nonnull
	AbstractTexture getSpecularTexture();
}
