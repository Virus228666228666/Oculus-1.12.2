package net.coderbot.iris.texture.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.io.FilenameUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;

public class TextureExporter {
	public static void exportTextures(String directory, String filename, int textureId, int mipLevel, int width, int height) {
		String extension = FilenameUtils.getExtension(filename);
		String baseName = filename.substring(0, filename.length() - extension.length() - 1);
		for (int level = 0; level <= mipLevel; ++level) {
			exportTexture(directory, baseName + "_" + level + "." + extension, textureId, level, width >> level, height >> level);
		}
	}

	public static void exportTexture(String directory, String filename, int textureId, int level, int width, int height) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int[] pixels = new int[width * height];
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
				buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
				buffer.put((byte) (pixel & 0xFF));         // Blue
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
			}
		}
		buffer.flip();

		GlStateManager.bindTexture(textureId);
//		bufferedImage.downloadTexture(level, false);
//		this.format.setPackPixelStoreState();
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
//		GlStateManager._getTexImage(3553, p_85046_, this.format.glFormat(), 5121, this.pixels);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, level, BufferedImage.TYPE_INT_ARGB, GL11.GL_UNSIGNED_BYTE, buffer);
//		if (p_85047_ && this.format.hasAlpha()) {
//			for(int var3 = 0; var3 < this.getHeight(); ++var3) {
//				for(int var4 = 0; var4 < this.getWidth(); ++var4) {
//					this.setPixelRGBA(var4, var3, this.getPixelRGBA(var4, var3) | 255 << this.format.alphaOffset());
//				}
//			}
//		}

		File dir = new File(Minecraft.getMinecraft().gameDir, directory);
		dir.mkdirs();
		File file = new File(dir, filename);

		// todo
//		Util.ioPool().execute(() -> {
//			try {
//				bufferedImage.writeToFile(file);
//			} catch (Exception var7) {
//				//
//			} finally {
//				bufferedImage.close();
//			}
//		});
		try {
			ImageIO.write(bufferedImage, "png", file);
		} catch (IOException e) {
			// todo: handle exception
		}

	}
}
