package net.coderbot.iris.gui.option;

import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class ShadowDistanceSliderButton extends GuiSlider {
	public ShadowDistanceSliderButton(GuiPageButtonList.GuiResponder guiResponder, int id, int x, int y, String name, float minValue, float maxValue, float currentValue) {
		super(guiResponder, id, x, y, name, minValue, maxValue, currentValue, (idIn, nameIn, value) -> {
			return new TextComponentTranslation("options.generic_value", new TextComponentTranslation(nameIn), (int) value).getFormattedText();
		});
	}

	@Override
	public boolean isMouseOver() {
		boolean actuallyActive = this.enabled;
		this.enabled = true;

		boolean mouseOver = super.isMouseOver();

		this.enabled = actuallyActive;
		return mouseOver;
	}
}
