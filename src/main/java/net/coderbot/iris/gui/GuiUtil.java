package net.coderbot.iris.gui;

//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
//import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
//import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
//import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.init.SoundEvents;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.opengl.GL11;

/**
 * Class serving as abstraction and
 * centralization for common GUI
 * rendering/other code calls.
 *
 * Helps allow for easier portability
 * to Minecraft 1.17 by abstracting
 * some code that will be changed.
 */
public final class GuiUtil {
	public static final ResourceLocation IRIS_WIDGETS_TEX = new ResourceLocation("iris", "textures/gui/widgets.png");
	private static final ITextComponent ELLIPSIS = new TextComponentString("...");

	private GuiUtil() {}

	private static Minecraft client() {
		return Minecraft.getMinecraft();
	}

	/**
	 * Binds Iris's widgets texture to be
	 * used for succeeding draw calls.
	 */
	public static void bindIrisWidgetsTexture() {
		client().getTextureManager().bindTexture(IRIS_WIDGETS_TEX);
	}

	/**
	 * Draws a textured rectangle at the specified position.
	 *
	 * @param x      The x position of the rectangle
	 * @param y      The y position of the rectangle
	 * @param u      The x coordinate on the texture to start drawing from
	 * @param v      The y coordinate on the texture to start drawing from
	 * @param width  The width of the rectangle
	 * @param height The height of the rectangle
	 */
	private static void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + height, 0).tex(u / 256.0F, (v + height) / 256.0F).endVertex();
		buffer.pos(x + width, y + height, 0).tex((u + width) / 256.0F, (v + height) / 256.0F).endVertex();
		buffer.pos(x + width, y, 0).tex((u + width) / 256.0F, v / 256.0F).endVertex();
		buffer.pos(x, y, 0).tex(u / 256.0F, v / 256.0F).endVertex();
		tessellator.draw();
	}

	/**
	 * Draws a textured rectangle with a subset of a texture at the specified position.
	 *
	 * @param x         The x position of the rectangle
	 * @param y         The y position of the rectangle
	 * @param uOffset   The x coordinate on the texture to start drawing from
	 * @param vOffset   The y coordinate on the texture to start drawing from
	 * @param width     The width of the rectangle
	 * @param height    The height of the rectangle
	 * @param textureWidth  The width of the texture
	 * @param textureHeight The height of the texture
	 */
	private static void drawTexturedModalRectWithOffset(int x, int y, int uOffset, int vOffset, int width, int height, int textureWidth, int textureHeight) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + height, 0).tex(uOffset / (float) textureWidth, (vOffset + height) / (float) textureHeight).endVertex();
		buffer.pos(x + width, y + height, 0).tex((uOffset + width) / (float) textureWidth, (vOffset + height) / (float) textureHeight).endVertex();
		buffer.pos(x + width, y, 0).tex((uOffset + width) / (float) textureWidth, vOffset / (float) textureHeight).endVertex();
		buffer.pos(x, y, 0).tex(uOffset / (float) textureWidth, vOffset / (float) textureHeight).endVertex();
		tessellator.draw();
	}

	/**
	 * Draws a button. Button textures must be mapped with the
	 * same coordinates as those on the vanilla widgets texture.
	 *
	 * @param x X position of the left of the button
	 * @param y Y position of the top of the button
	 * @param width Width of the button, maximum 398
	 * @param height Height of the button, maximum 20
	 * @param hovered Whether the button is being hovered over with the mouse
	 * @param disabled Whether the button should use the "disabled" texture
	 */
	public static void drawButton(int x, int y, int width, int height, boolean hovered, boolean disabled) {
		// Create variables for half of the width and height.
		// Will not be exact when width and height are odd, but
		// that case is handled within the draw calls.
		int halfWidth = width / 2;
		int halfHeight = height / 2;

		// V offset for which button texture to use
		int vOffset = disabled ? 46 : hovered ? 86 : 66;

		// Sets RenderSystem to use solid white as the tint color for blend mode, and enables blend mode
//		RenderSystem.blendColor(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableBlend();

		// Sets RenderSystem to be able to use textures when drawing
		GlStateManager.enableTexture2D();

		// Top left section
		drawTexturedModalRectWithOffset(x, y, 0, vOffset, halfWidth, halfHeight, 256, 256);
		// Top right section
		drawTexturedModalRectWithOffset(x + halfWidth, y, 200 - (width - halfWidth), vOffset, width - halfWidth, halfHeight, 256, 256);
		// Bottom left section
		drawTexturedModalRectWithOffset(x, y + halfHeight, 0, vOffset + (20 - (height - halfHeight)), halfWidth, height - halfHeight, 256, 256);
		// Bottom right section
		drawTexturedModalRectWithOffset(x + halfWidth, y + halfHeight, 200 - (width - halfWidth), vOffset + (20 - (height - halfHeight)), width - halfWidth, height - halfHeight, 256, 256);
	}

	/**
	 * Draws a translucent black panel
	 * with a light border.
	 *
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 * @param width The width of the panel
	 * @param height The height of the panel
	 */
	public static void drawPanel(int x, int y, int width, int height) {
		int borderColor = 0xDEDEDEDE;
		int innerColor = 0xDE000000;

		// Top border section
		Gui.drawRect(x, y, x + width, y + 1, borderColor);
		// Bottom border section
		Gui.drawRect(x, (y + height) - 1, x + width, y + height, borderColor);
		// Left border section
		Gui.drawRect(x, y + 1, x + 1, (y + height) - 1, borderColor);
		// Right border section
		Gui.drawRect((x + width) - 1, y + 1, x + width, (y + height) - 1, borderColor);
		// Inner section
		Gui.drawRect(x + 1, y + 1, (x + width) - 1, (y + height) - 1, innerColor);
	}

	/**
	 * Draws a text with a panel behind it.
	 *
	 * @param text The text component to draw
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 */
	public static void drawTextPanel(FontRenderer font, String text, int x, int y) {
		drawPanel(x, y, font.getStringWidth(text) + 8, 16);
		font.drawStringWithShadow(text, x + 4, y + 4, 0xFFFFFF);
	}

	/**
	 * Shorten a text to a specific length, adding an ellipsis (...)
	 * to the end if shortened.
	 *
	 * Text may lose formatting.
	 *
	 * @param font Font to use for determining the width of text
	 * @param text Text to shorten
	 * @param width Width to shorten text to
	 * @return a shortened text
	 */
	public static String shortenText(FontRenderer font, String text, int width) {
		if (font.getStringWidth(text) > width) {
			return font.trimStringToWidth(text, width - font.getStringWidth("...")) + "...";
		}
		return text;
	}


	/**
	 * Creates a new translated text, if a translation
	 * is present. If not, will return the default text
	 * component passed.
	 *
	 * @param defaultText Default text to use if no translation is found
	 * @param translationDesc Translation key to try and use
	 * @param format Formatting arguments for the translated text, if created
	 * @return the translated text if found, otherwise the default provided
	 */
	public static ITextComponent translateOrDefault(ITextComponent defaultText, String translationDesc, Object ... format) {
		if (I18n.hasKey(translationDesc)) {
			return new TextComponentTranslation(translationDesc, format);
		}
		return defaultText;
	}

	/**
	 * Plays the {@code UI_BUTTON_CLICK} sound event as a
	 * master sound effect.
	 *
	 * Used in non-{@code ButtonWidget} UI elements upon click
	 * or other action.
	 */
	public static void playButtonClickSound() {
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	/**
	 * A class representing a section of a
	 * texture, to be easily drawn in GUIs.
	 */
	public static class Icon {
		public static final Icon SEARCH = new Icon(0, 0, 7, 8);
		public static final Icon CLOSE = new Icon(7, 0, 5, 6);
		public static final Icon REFRESH = new Icon(12, 0, 10, 10);
		public static final Icon EXPORT = new Icon(22, 0, 7, 8);
		public static final Icon EXPORT_COLORED = new Icon(29, 0, 7, 8);
		public static final Icon IMPORT = new Icon(22, 8, 7, 8);
		public static final Icon IMPORT_COLORED = new Icon(29, 8, 7, 8);

		private final int u;
		private final int v;
		@Getter
        private final int width;
		@Getter
        private final int height;

		public Icon(int u, int v, int width, int height) {
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}

		/**
		 * Draws this icon to the screen at the specified coordinates.
		 *
		 * @param x The x position to draw the icon at (left)
		 * @param y The y position to draw the icon at (top)
		 */
		/**
		 * Draws this icon to the screen at the specified coordinates.
		 *
		 * @param x The x position to draw the icon at (left)
		 * @param y The y position to draw the icon at (top)
		 */
		public void draw(int x, int y) {
			// Sets OpenGL to use solid white as the tint color for blend mode, and enables blend mode
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableBlend();

			// Sets OpenGL to be able to use textures when drawing
			GlStateManager.enableTexture2D();

			// Draw the texture to the screen
			drawTexturedModalRect(x, y, u, v, width, height);
		}
    }
}
