package net.coderbot.iris.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import java.util.List;
import java.util.ArrayList;

public class FeatureMissingErrorScreen extends GuiScreen {
	private final GuiScreen parent;
	private List<String> messageLines = new ArrayList<>();
	private final String title;
	private final String messageTemp;

	public FeatureMissingErrorScreen(GuiScreen parent, String title, String message) {
		this.parent = parent;
		this.title = title;
		this.messageTemp = message;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.messageLines = this.fontRenderer.listFormattedStringToWidth(this.messageTemp, this.width - 50);
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 140, I18n.format("gui.back")));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			this.mc.displayGuiScreen(this.parent);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 90, 0xFFFFFF);

		int y = 110;
		for (String line : this.messageLines) {
			this.drawCenteredString(this.fontRenderer, line, this.width / 2, y, 0xFFFFFF);
			y += this.fontRenderer.FONT_HEIGHT;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
