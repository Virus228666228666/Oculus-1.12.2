package net.coderbot.iris.rendertarget;

import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.image.BufferedImage;

public class BufferedImageBackedSingleColorTexture extends DynamicTexture {

	public BufferedImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(create((alpha & 255) << 24 | (blue & 255) << 16 | (green & 255) << 8 | (red & 255)));
	}

	public BufferedImageBackedSingleColorTexture(int rgba) {
		this(rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
	}

	private static BufferedImage create(int color) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, color);
		return image;
	}
}
