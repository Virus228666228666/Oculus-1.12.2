package net.coderbot.iris.texture.mipmap;

import java.awt.image.BufferedImage;

public abstract class AbstractMipmapGenerator implements CustomMipmapGenerator {
	@Override
	public BufferedImage[] generateMipLevels(BufferedImage image, int mipLevel) {
		BufferedImage[] images = new BufferedImage[mipLevel + 1];
		images[0] = image;
		if (mipLevel > 0) {
			for (int level = 1; level <= mipLevel; ++level) {
				BufferedImage prevMipmap = images[level - 1];
				BufferedImage mipmap = new BufferedImage(prevMipmap.getWidth() >> 1, prevMipmap.getHeight() >> 1, BufferedImage.TYPE_INT_ARGB);
				int width = mipmap.getWidth();
				int height = mipmap.getHeight();
				for (int x = 0; x < width; ++x) {
					for (int y = 0; y < height; ++y) {
						mipmap.setRGB(x, y, blend(
								prevMipmap.getRGB(x * 2, y * 2),
								prevMipmap.getRGB(x * 2 + 1, y * 2),
								prevMipmap.getRGB(x * 2, y * 2 + 1),
								prevMipmap.getRGB(x * 2 + 1, y * 2 + 1)
						));
					}
				}
				images[level] = mipmap;
			}
		}
		return images;
	}

	public abstract int blend(int c0, int c1, int c2, int c3);
}
