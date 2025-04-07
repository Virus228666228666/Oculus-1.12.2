package net.coderbot.iris.compat.sodium.impl.options;

import java.io.IOException;

import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.colorspace.ColorSpace;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class IrisSodiumOptions {
	public static OptionImpl<GameSettings, Integer> createMaxShadowDistanceSlider(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<GameSettings, Integer> maxShadowDistanceSlider = OptionImpl.createBuilder(int.class, vanillaOpts)
				.setName(new TextComponentTranslation("options.iris.shadowDistance"))
				.setTooltip(new TextComponentTranslation("options.iris.shadowDistance.sodium_tooltip"))
				.setControl(option -> new SliderControl(option, 0, 32, 1, ControlValueFormatter.quantity("options.chunks")))
				.setBinding((options, value) -> {
							IrisVideoSettings.shadowDistance = value;
							try {
								Iris.getIrisConfig().save();
							} catch (IOException e) {
								e.printStackTrace();
							}
						},
						options -> IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance))
				.setImpact(OptionImpact.HIGH)
				.setEnabled(true)
				.build();

		((OptionImplExtended) maxShadowDistanceSlider).iris$dynamicallyEnable(IrisVideoSettings::isShadowDistanceSliderEnabled);

		return maxShadowDistanceSlider;
	}

	public static OptionImpl<GameSettings, ColorSpace> createColorSpaceButton(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<GameSettings, ColorSpace> colorSpace = OptionImpl.createBuilder(ColorSpace.class, vanillaOpts)
				.setName(new TextComponentTranslation("options.iris.colorSpace"))
				.setTooltip(new TextComponentTranslation("options.iris.colorSpace.sodium_tooltip"))
				.setControl(option -> new CyclingControl<>(option, ColorSpace.class,
						new ITextComponent[] { new TextComponentString("SRGB"), new TextComponentString("DCI_P3"), new TextComponentString("Display P3"), new TextComponentString("REC2020"), new TextComponentString("Adobe RGB") }))
				.setBinding((options, value) -> {
							IrisVideoSettings.colorSpace = value;
							try {
								Iris.getIrisConfig().save();
							} catch (IOException e) {
								e.printStackTrace();
							}
						},
						options -> IrisVideoSettings.colorSpace)
				.setImpact(OptionImpact.LOW)
				.setEnabled(true)
				.build();


		return colorSpace;
	}
}
