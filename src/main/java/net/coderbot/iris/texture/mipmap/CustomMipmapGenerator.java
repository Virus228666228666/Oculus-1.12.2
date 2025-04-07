package net.coderbot.iris.texture.mipmap;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

public interface CustomMipmapGenerator {
	BufferedImage[] generateMipLevels(BufferedImage image, int mipLevel);

	public interface Provider {
		@Nullable
		CustomMipmapGenerator getMipmapGenerator(int width);
	}
}
