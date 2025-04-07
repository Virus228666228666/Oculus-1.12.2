package net.coderbot.iris.gui.option;

import java.io.IOException;

import net.coderbot.iris.Iris;
import net.coderbot.iris.colorspace.ColorSpace;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class IrisVideoSettings {
	public static int shadowDistance = 32;

	// TODO: Tell the user to check in the shader options once that's supported.
	private static final ITextComponent DISABLED_TOOLTIP = new TextComponentTranslation("options.iris.shadowDistance.disabled");
	private static final ITextComponent ENABLED_TOOLTIP = new TextComponentTranslation("options.iris.shadowDistance.enabled");
	public static ColorSpace colorSpace = ColorSpace.SRGB;

	public static int getOverriddenShadowDistance(int base) {
		return Iris.getPipelineManager().getPipeline()
				.map(pipeline -> pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(base))
				.orElse(base);
	}

	public static boolean isShadowDistanceSliderEnabled() {
		return Iris.getPipelineManager().getPipeline()
				.map(pipeline -> !pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent())
				.orElse(true);
	}

	public static final ShadowDistanceOption RENDER_DISTANCE = new ShadowDistanceOption(
			"options.iris.shadowDistance", 0.0D, 32.0D, 1.0F,
			(gameOptions) -> {
				return (double) getOverriddenShadowDistance(shadowDistance);
			},
			(gameOptions, viewDistance) -> {
				double outputShadowDistance = viewDistance;
				shadowDistance = (int) outputShadowDistance;
				try {
					Iris.getIrisConfig().save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			},
			(gameOptions, option) -> {
				int d = (int) option.get(gameOptions);

				WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

				ITextComponent tooltip;

				if (pipeline != null) {
					d = pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(d);

					if (pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent()) {
						tooltip = new TextComponentTranslation("options.iris.shadowDistance.disabled");
					} else {
						tooltip = new TextComponentTranslation("options.iris.shadowDistance.enabled");
					}
				} else {
					tooltip = new TextComponentTranslation("options.iris.shadowDistance.enabled");
				}

				// todo option.setTooltip(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(tooltip.getFormattedText(), 200));

				if (d <= 0.0) {
					return new TextComponentTranslation("options.generic_value", new TextComponentTranslation("options.iris.shadowDistance"), "0 (disabled)");
				} else {
					return new TextComponentTranslation("options.generic_value", new TextComponentTranslation("options.iris.shadowDistance"), new TextComponentTranslation("options.chunks", d));
				}
			}
	);

}
