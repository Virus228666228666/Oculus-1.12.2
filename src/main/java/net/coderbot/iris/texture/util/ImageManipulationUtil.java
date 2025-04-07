package net.coderbot.iris.texture.util;

import java.awt.image.BufferedImage;

public class ImageManipulationUtil {
	public static BufferedImage scaleNearestNeighbor(BufferedImage image, int newWidth, int newHeight) {
		BufferedImage scaled = new BufferedImage(newWidth, newHeight, image.getType());
		float xScale = (float) newWidth / image.getWidth();
		float yScale = (float) newHeight / image.getHeight();
		for (int y = 0; y < newHeight; ++y) {
			for (int x = 0; x < newWidth; ++x) {
				float unscaledX = (x + 0.5f) / xScale;
				float unscaledY = (y + 0.5f) / yScale;
				scaled.setRGB(x, y, image.getRGB((int) unscaledX, (int) unscaledY));
			}
		}
		return scaled;
	}

	public static BufferedImage scaleBilinear(BufferedImage image, int newWidth, int newHeight) {
		BufferedImage scaled = new BufferedImage(newWidth, newHeight, image.getType());
		float xScale = (float) newWidth / image.getWidth();
		float yScale = (float) newHeight / image.getHeight();
		for (int y = 0; y < newHeight; ++y) {
			for (int x = 0; x < newWidth; ++x) {
				float unscaledX = (x + 0.5f) / xScale;
				float unscaledY = (y + 0.5f) / yScale;

				int x1 = Math.round(unscaledX);
				int y1 = Math.round(unscaledY);
				int x0 = x1 - 1;
				int y0 = y1 - 1;

				boolean x0valid = true;
				boolean y0valid = true;
				boolean x1valid = true;
				boolean y1valid = true;
				if (x0 < 0) {
					x0valid = false;
				}
				if (y0 < 0) {
					y0valid = false;
				}
				if (x1 >= image.getWidth()) {
					x1valid = false;
				}
				if (y1 >= image.getHeight()) {
					y1valid = false;
				}

				int finalColor = 0;
				if (x0valid & y0valid & x1valid & y1valid) {
					float leftWeight = (x1 + 0.5f) - unscaledX;
					float rightWeight = unscaledX - (x0 + 0.5f);
					float topWeight = (y1 + 0.5f) - unscaledY;
					float bottomWeight = unscaledY - (y0 + 0.5f);

					float weightTL = leftWeight * topWeight;
					float weightTR = rightWeight * topWeight;
					float weightBL = leftWeight * bottomWeight;
					float weightBR = rightWeight * bottomWeight;

					int colorTL = image.getRGB(x0, y0);
					int colorTR = image.getRGB(x1, y0);
					int colorBL = image.getRGB(x0, y1);
					int colorBR = image.getRGB(x1, y1);

					finalColor = blendColor(colorTL, colorTR, colorBL, colorBR, weightTL, weightTR, weightBL, weightBR);
				} else if (x0valid & x1valid) {
					float leftWeight = (x1 + 0.5f) - unscaledX;
					float rightWeight = unscaledX - (x0 + 0.5f);

					int validY = y0valid ? y0 : y1;
					int colorLeft = image.getRGB(x0, validY);
					int colorRight = image.getRGB(x1, validY);

					finalColor = blendColor(colorLeft, colorRight, leftWeight, rightWeight);
				} else if (y0valid & y1valid) {
					float topWeight = (y1 + 0.5f) - unscaledY;
					float bottomWeight = unscaledY - (y0 + 0.5f);

					int validX = x0valid ? x0 : x1;
					int colorTop = image.getRGB(validX, y0);
					int colorBottom = image.getRGB(validX, y1);

					finalColor = blendColor(colorTop, colorBottom, topWeight, bottomWeight);
				} else {
					finalColor = image.getRGB(x0valid ? x0 : x1, y0valid ? y0 : y1);
				}
				scaled.setRGB(x, y, finalColor);
			}
		}
		return scaled;
	}

	private static int blendColor(int c0, int c1, int c2, int c3, float w0, float w1, float w2, float w3) {
		return combine(
				blendChannel((c0 >> 24) & 0xFF, (c0 >> 24) & 0xFF, (c0 >> 24) & 0xFF, (c0 >> 24) & 0xFF, w0, w1, w2, w3),
				blendChannel((c0 & 0xFF), (c1 & 0xFF), (c2 & 0xFF), (c3 & 0xFF), w0, w1, w2, w3),
				blendChannel((c0 >> 8) & 0xFF, (c1 >> 8) & 0xFF, (c2 >> 8) & 0xFF, (c3 >> 8) & 0xFF, w0, w1, w2, w3),
				blendChannel((c0 >> 16) & 0xFF, (c1 >> 16) & 0xFF, (c2 >> 16) & 0xFF, (c3 >> 16) & 0xFF, w0, w1, w2, w3)
		);
	}

	private static int blendChannel(int v0, int v1, int v2, int v3, float w0, float w1, float w2, float w3) {
		return Math.round(v0 * w0 + v1 * w1 + v2 * w2 + v3 * w3);
	}

	private static int blendColor(int c0, int c1, float w0, float w1) {
		return combine(
				blendChannel((c0 >> 24) & 0xFF, (c0 >> 24) & 0xFF, w0, w1),
				blendChannel((c0 & 0xFF), (c1 & 0xFF), w0, w1),
				blendChannel((c0 >> 8) & 0xFF, (c1 >> 8) & 0xFF, w0, w1),
				blendChannel((c0 >> 16) & 0xFF, (c1 >> 16) & 0xFF, w0, w1)
		);
	}

	private static int blendChannel(int v0, int v1, float w0, float w1) {
		return Math.round(v0 * w0 + v1 * w1);
	}

	private static int combine(int alpha, int blue, int green, int red) {
		alpha = (alpha & 0xFF) << 24;
		red = (red & 0xFF) << 16;
		green = (green & 0xFF) << 8;
		blue = blue & 0xFF;

		return alpha | red | green | blue;
	}
}
