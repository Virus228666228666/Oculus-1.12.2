package net.coderbot.iris.gl.blending;

//import com.mojang.blaze3d.platform.GlStateManager;

import lombok.Getter;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.minecraft.client.renderer.GlStateManager;

public class DepthColorStorage {
	private static boolean originalDepthEnable;
	private static ColorMask originalColor;
	@Getter
    private static boolean depthColorLocked;

    public static void disableDepthColor() {
		if (!depthColorLocked) {
			// Only save the previous state if the depth and color mask wasn't already locked
			GlStateManager.ColorMask colorMask = GlStateManagerAccessor.getCOLOR_MASK();
			GlStateManager.DepthState depthState = GlStateManagerAccessor.getDEPTH();

			originalDepthEnable = depthState.maskEnabled;
			originalColor = new ColorMask(colorMask.red, colorMask.green, colorMask.blue, colorMask.alpha);
		}

		depthColorLocked = false;

		GlStateManager.depthMask(false);
		GlStateManager.colorMask(false, false, false, false);

		depthColorLocked = true;
	}

	public static void deferDepthEnable(boolean enabled) {
		originalDepthEnable = enabled;
	}

	public static void deferColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
		originalColor = new ColorMask(red, green, blue, alpha);
	}

	public static void unlockDepthColor() {
		if (!depthColorLocked) {
			return;
		}

		depthColorLocked = false;

		if (originalDepthEnable) {
			GlStateManager.depthMask(true);
		} else {
			GlStateManager.depthMask(false);
		}

		GlStateManager.colorMask(originalColor.isRedMasked(), originalColor.isGreenMasked(), originalColor.isBlueMasked(), originalColor.isAlphaMasked());
	}
}
