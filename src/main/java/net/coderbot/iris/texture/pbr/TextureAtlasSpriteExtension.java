package net.coderbot.iris.texture.pbr;

import javax.annotation.Nullable;

public interface TextureAtlasSpriteExtension {
	@Nullable
	PBRSpriteHolder getPBRHolder();

	PBRSpriteHolder getOrCreatePBRHolder();
}
