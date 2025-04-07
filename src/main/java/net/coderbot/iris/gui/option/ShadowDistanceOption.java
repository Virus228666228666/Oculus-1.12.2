package net.coderbot.iris.gui.option;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.Getter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.client.Minecraft;

public class ShadowDistanceOption {
	@Getter
	private final String name;
	private final double minValue;
	private final double maxValue;
	private final float step;
	private final Function<GameSettings, Double> getter;
	private final BiConsumer<GameSettings, Double> setter;
	private final BiFunction<GameSettings, ShadowDistanceOption, ITextComponent> displayStringGetter;

	public ShadowDistanceOption(String name, double minValue, double maxValue, float step,
								Function<GameSettings, Double> getter,
								BiConsumer<GameSettings, Double> setter,
								BiFunction<GameSettings, ShadowDistanceOption, ITextComponent> displayStringGetter) {
		this.name = name;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.step = step;
		this.getter = getter;
		this.setter = setter;
		this.displayStringGetter = displayStringGetter;
	}

	public GuiButton createButton(GameSettings options, int x, int y, int width) {
		float currentValue = getter.apply(options).floatValue();
		GuiButton button = new ShadowDistanceSliderButton(new GuiResponder(), 0, x, y, name, (float) minValue, (float) maxValue, currentValue);
		button.enabled = IrisVideoSettings.isShadowDistanceSliderEnabled();
		return button;
	}

	private class GuiResponder implements GuiPageButtonList.GuiResponder {
		@Override
		public void setEntryValue(int id, boolean value) {

		}

		@Override
		public void setEntryValue(int id, String value) {

		}

		@Override
		public void setEntryValue(int id, float value) {
			setter.accept(Minecraft.getMinecraft().gameSettings, (double) value);
		}
	}

	public ITextComponent getDisplayString(GameSettings options) {
		return displayStringGetter.apply(options, this);
	}

	public double get(GameSettings options) {
		return getter.apply(options);
	}

	public void set(GameSettings options, double value) {
		setter.accept(options, value);
	}
}
