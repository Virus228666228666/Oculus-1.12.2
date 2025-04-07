package net.coderbot.iris.texture.pbr;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nullable;

public class PBRSpriteHolder {
	protected TextureAtlasSprite normalSprite;
	protected TextureAtlasSprite specularSprite;

	@Nullable
	public TextureAtlasSprite getNormalSprite() {
		return normalSprite;
	}

	@Nullable
	public TextureAtlasSprite getSpecularSprite() {
		return specularSprite;
	}

	public void setNormalSprite(TextureAtlasSprite sprite) {
		normalSprite = sprite;
	}

	public void setSpecularSprite(TextureAtlasSprite sprite) {
		specularSprite = sprite;
	}
}
