package net.coderbot.iris.rendertarget;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Random;

import lombok.Getter;
import net.coderbot.iris.mixin.texture.DynamicTextureAccessor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

@Getter
public class BufferedImageBackedNoiseTexture extends DynamicTexture {
	public BufferedImageBackedNoiseTexture(int size) {
		super(create(size));
	}

	private static BufferedImage create(int size) {
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Random random = new Random(0);

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				int color = random.nextInt() | (255 << 24);

				image.setRGB(x, y, color);
			}
		}

		return image;
	}

	@Override
	public void updateDynamicTexture() {
		// create the image
		int[] textureData = this.getTextureData();
		int width = ((DynamicTextureAccessor) this).getWidth();
		int height = ((DynamicTextureAccessor) this).getHeight();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, textureData, 0, width);

		// bind
		GlStateManager.bindTexture(this.getGlTextureId());

		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

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

		// upload
		// image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, false);
		// private void _upload(int 0, int 0, int 0, int 0, int 0, int p_85096_, int p_85097_, boolean p_85098_, boolean p_85099_, boolean p_85100_, boolean p_85101_) {
		//        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		//        this.checkAllocated();
		//        setFilter(p_85098_, p_85100_);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_CLAMP);
		//        if (p_85096_ == this.getWidth()) {
		//            GlStateManager._pixelStore(3314, 0);
		GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
		//        } else {
		//            GlStateManager._pixelStore(3314, this.getWidth());
		//        }
		//
		//        GlStateManager._pixelStore(3316, p_85094_);
		//        GlStateManager._pixelStore(3315, p_85095_);
		GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
		GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
		//        this.format.setUnpackPixelStoreState();
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
		//        GlStateManager._texSubImage2D(3553, p_85091_, p_85092_, p_85093_, p_85096_, p_85097_, this.format.glFormat(), 5121, this.pixels);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		//        if (p_85099_) {
		//            GlStateManager._texParameter(3553, 10242, 33071);
		//            GlStateManager._texParameter(3553, 10243, 33071);
		//        }
		//
		//        if (p_85101_) {
		//            this.close();
		//        }
		//
		//    }
	}
}
