package net.coderbot.iris.gui.option;

import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.TextComponentTranslation;

public class ShaderPackSelectionButtonOption extends GuiButton {
	private final GuiScreen parent;
	private final Minecraft client;

	public ShaderPackSelectionButtonOption(GuiScreen parent, Minecraft client) {
		super(0, 0, 0, 150, 20, new TextComponentTranslation("options.iris.shaderPackSelection").getFormattedText());
		this.parent = parent;
		this.client = client;
	}

	public void onClick(double mouseX, double mouseY) {
		this.client.displayGuiScreen(new ShaderPackScreen(this.parent));
	}

	public GuiButton createButton(GameSettings options, int x, int y, int width) {
		return new ShaderPackSelectionButtonOption(this.parent, this.client);
	}
}
