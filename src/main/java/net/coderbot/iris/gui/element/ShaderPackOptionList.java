package net.coderbot.iris.gui.element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.FileDialogUtil;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.OptionMenuConstructor;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class ShaderPackOptionList extends IrisObjectSelectionList<ShaderPackOptionList.BaseEntry> {
	private final List<AbstractElementWidget<?>> elementWidgets = new ArrayList<>();
	private final ShaderPackScreen screen;
	@Getter
    private final NavigationController navigation;
	private OptionMenuContainer container;

	public ShaderPackOptionList(ShaderPackScreen screen, NavigationController navigation, ShaderPack pack, Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 24);
		this.navigation = navigation;
		this.screen = screen;

		applyShaderPack(pack);
	}

	public void applyShaderPack(ShaderPack pack) {
		this.container = pack.getMenuContainer();
	}

	public void rebuild() {
		this.clearEntries();
//		todo this.setScrollAmount(0);
		OptionMenuConstructor.constructAndApplyToScreen(this.container, this.screen, this, navigation);
	}

	public void refresh() {
		this.elementWidgets.forEach(widget -> widget.init(this.screen, this.navigation));
	}

	@Override
	public int getListWidth() {
		return Math.min(400, width - 12);
	}

	public void addHeader(ITextComponent text, boolean backButton) {
		this.addEntry(new HeaderEntry(this.screen, this.navigation, text, backButton));
	}

	public void addWidgets(int columns, List<AbstractElementWidget<?>> elements) {
		this.elementWidgets.addAll(elements);

		List<AbstractElementWidget<?>> row = new ArrayList<>();
		for (AbstractElementWidget<?> element : elements) {
			row.add(element);

			if (row.size() >= columns) {
				this.addEntry(new ElementRowEntry(screen, this.navigation, row));
				row = new ArrayList<>(); // Clearing the list would affect the row entry created above
			}
		}

		if (!row.isEmpty()) {
			while (row.size() < columns) {
				row.add(AbstractElementWidget.EMPTY);
			}

			this.addEntry(new ElementRowEntry(screen, this.navigation, row));
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		if (mouseX >= super.getScrollBarX()) {
			return super.mouseClicked(mouseX, mouseY, mouseEvent);
		}
		return false;
	}

    public abstract static class BaseEntry implements GuiListExtended.IGuiListEntry {
		protected final NavigationController navigation;

		protected BaseEntry(NavigationController navigation) {
			this.navigation = navigation;
		}
	}

	public static class HeaderEntry extends BaseEntry {
		public static final ITextComponent BACK_BUTTON_TEXT = new TextComponentString("< ").appendSibling(new TextComponentTranslation("options.iris.back").setStyle(new Style().setItalic(true)));
		public static final ITextComponent RESET_BUTTON_TEXT_INACTIVE = new TextComponentTranslation("options.iris.reset").setStyle(new Style().setColor(TextFormatting.GRAY));
		public static final ITextComponent RESET_BUTTON_TEXT_ACTIVE = new TextComponentTranslation("options.iris.reset").setStyle(new Style().setColor(TextFormatting.YELLOW));

		public static final ITextComponent RESET_HOLD_SHIFT_TOOLTIP = new TextComponentTranslation("options.iris.reset.tooltip.holdShift").setStyle(new Style().setColor(TextFormatting.GOLD));
		public static final ITextComponent RESET_TOOLTIP = new TextComponentTranslation("options.iris.reset.tooltip").setStyle(new Style().setColor(TextFormatting.RED));
		public static final ITextComponent IMPORT_TOOLTIP = new TextComponentTranslation("options.iris.importSettings.tooltip")
				.setStyle(new Style().setColor(TextFormatting.AQUA));
		public static final ITextComponent EXPORT_TOOLTIP = new TextComponentTranslation("options.iris.exportSettings.tooltip")
				.setStyle(new Style().setColor(TextFormatting.GOLD));

		private static final int MIN_SIDE_BUTTON_WIDTH = 42;
		private static final int BUTTON_HEIGHT = 16;

		private final ShaderPackScreen screen;
		private final @Nullable IrisElementRow backButton;
		private final IrisElementRow utilityButtons = new IrisElementRow();
		private final IrisElementRow.TextButtonElement resetButton;
		private final IrisElementRow.IconButtonElement importButton;
		private final IrisElementRow.IconButtonElement exportButton;
		private final ITextComponent text;

		public HeaderEntry(ShaderPackScreen screen, NavigationController navigation, ITextComponent text, boolean hasBackButton) {
			super(navigation);

			if (hasBackButton) {
				this.backButton = new IrisElementRow().add(
						new IrisElementRow.TextButtonElement(BACK_BUTTON_TEXT.getFormattedText(), this::backButtonClicked),
						Math.max(MIN_SIDE_BUTTON_WIDTH, Minecraft.getMinecraft().fontRenderer.getStringWidth(BACK_BUTTON_TEXT.getFormattedText()) + 8)
				);
			} else {
				this.backButton = null;
			}

			this.resetButton = new IrisElementRow.TextButtonElement(
					RESET_BUTTON_TEXT_INACTIVE.getFormattedText(), this::resetButtonClicked);
			this.importButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.IMPORT, GuiUtil.Icon.IMPORT_COLORED, this::importSettingsButtonClicked);
			this.exportButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.EXPORT, GuiUtil.Icon.EXPORT_COLORED, this::exportSettingsButtonClicked);

			this.utilityButtons
					.add(this.importButton, 15)
					.add(this.exportButton, 15)
					.add(this.resetButton, Math.max(MIN_SIDE_BUTTON_WIDTH, Minecraft.getMinecraft().fontRenderer.getStringWidth(RESET_BUTTON_TEXT_INACTIVE.getFormattedText()) + 8));

			this.screen = screen;
			this.text = text;
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			// todo: Draw dividing line
//			fill(x - 3, (y + entryHeight) - 2, x + entryWidth, (y + entryHeight) - 1, 0x66BEBEBE);

			FontRenderer font = Minecraft.getMinecraft().fontRenderer;

			// Draw header text
			font.drawStringWithShadow(text.getFormattedText(), x + (int)(listWidth * 0.5), y + 5, 0xFFFFFF);

			GuiUtil.bindIrisWidgetsTexture();

			// Draw back button if present
			if (this.backButton != null) {
				backButton.render(x, y, BUTTON_HEIGHT, mouseX, mouseY, partialTicks, isSelected);
			}

			boolean shiftDown = GuiScreen.isShiftKeyDown();

			// Set the appearance of the reset button
			this.resetButton.disabled = !shiftDown;
			this.resetButton.text = (shiftDown ? RESET_BUTTON_TEXT_ACTIVE : RESET_BUTTON_TEXT_INACTIVE).getFormattedText();

			// Draw the utility buttons
			this.utilityButtons.renderRightAligned((x + listWidth) - 3, y, BUTTON_HEIGHT, mouseX, mouseY, partialTicks, isSelected);

			// Draw the reset button's tooltip
			if (this.resetButton.isHovered()) {
				ITextComponent tooltip = shiftDown ? RESET_TOOLTIP : RESET_HOLD_SHIFT_TOOLTIP;
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, tooltip);
			}
			// Draw the import/export button tooltips
			if (this.importButton.isHovered()) {
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, IMPORT_TOOLTIP);
			}
			if (this.exportButton.isHovered()) {
				queueBottomRightAnchoredTooltip(mouseX, mouseY, font, EXPORT_TOOLTIP);
			}
		}

		private void queueBottomRightAnchoredTooltip(int x, int y, FontRenderer font, ITextComponent text) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(
					font, text.getFormattedText(),
					x - (font.getStringWidth(text.getFormattedText()) + 10), y - 16
			));
		}

		@Override
		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
			return this.mouseClicked(mouseX, mouseY, mouseEvent);
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

		}

		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			boolean backButtonResult = backButton != null && backButton.mouseClicked(mouseX, mouseY, button);
			boolean utilButtonResult = utilityButtons.mouseClicked(mouseX, mouseY, button);

			return backButtonResult || utilButtonResult;
		}

		private boolean backButtonClicked(IrisElementRow.TextButtonElement button) {
			this.navigation.back();
			GuiUtil.playButtonClickSound();

			return true;
		}

		private boolean resetButtonClicked(IrisElementRow.TextButtonElement button) {
			if (GuiScreen.isShiftKeyDown()) {
				Iris.resetShaderPackOptionsOnNextReload();
				this.screen.applyChanges();
				GuiUtil.playButtonClickSound();

				return true;
			}

			return false;
		}

		private boolean importSettingsButtonClicked(IrisElementRow.IconButtonElement button) {
			GuiUtil.playButtonClickSound();

			// Invalid state to be in
			if (!Iris.getCurrentPack().isPresent()) {
				return false;
			}

			// Displaying a dialog when the game is full-screened can cause severe issues
			// https://github.com/IrisShaders/Iris/issues/1258
			if (Minecraft.getMinecraft().isFullScreen()) {
				this.screen.displayNotification(
						new TextComponentTranslation("options.iris.mustDisableFullscreen")
								.setStyle(new Style().setColor(TextFormatting.RED).setBold(true)));
				return false;
			}

			final ShaderPackScreen originalScreen = this.screen; // Also used to prevent invalid state

			FileDialogUtil.fileSelectDialog(
							FileDialogUtil.DialogType.OPEN, "Import Shader Settings from File",
							Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt"),
							"Shader Pack Settings (.txt)", "*.txt")
					.whenComplete((path, err) -> {
						if (err != null) {
							Iris.logger.error("Error selecting shader settings from file", err);

							return;
						}

						if (Minecraft.getMinecraft().currentScreen == originalScreen) {
							path.ifPresent(originalScreen::importPackOptions);
						}
					});

			return true;
		}

		private boolean exportSettingsButtonClicked(IrisElementRow.IconButtonElement button) {
			GuiUtil.playButtonClickSound();

			// Invalid state to be in
			if (!Iris.getCurrentPack().isPresent()) {
				return false;
			}

			// Displaying a dialog when the game is full-screened can cause severe issues
			// https://github.com/IrisShaders/Iris/issues/1258
			if (Minecraft.getMinecraft().isFullScreen()) {
				this.screen.displayNotification(
						new TextComponentTranslation("options.iris.mustDisableFullscreen")
								.setStyle(new Style().setColor(TextFormatting.RED).setBold(true)));
				return false;
			}

			FileDialogUtil.fileSelectDialog(
							FileDialogUtil.DialogType.SAVE, "Export Shader Settings to File",
							Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt"),
							"Shader Pack Settings (.txt)", "*.txt")
					.whenComplete((path, err) -> {
						if (err != null) {
							Iris.logger.error("Error selecting file to export shader settings", err);

							return;
						}

						path.ifPresent(p -> {
							Properties toSave = new Properties();

							// Dirty way of getting the currently applied settings as a Properties, directly
							// opens and copies out of the saved settings file if it is present
							Path sourceTxtPath = Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt");
							if (Files.exists(sourceTxtPath)) {
								try (InputStream in = Files.newInputStream(sourceTxtPath)) {
									toSave.load(in);
								} catch (IOException ignored) {}
							}

							// Save properties to user determined file
							try (OutputStream out = Files.newOutputStream(p)) {
								toSave.store(out, null);
							} catch (IOException e) {
								Iris.logger.error("Error saving properties to \"" + p + "\"", e);
							}
						});
					});

			return true;
		}
	}

	public static class ElementRowEntry extends BaseEntry {
		private final List<AbstractElementWidget<?>> widgets;
		private final ShaderPackScreen screen;

		private int cachedWidth;
		private int cachedPosX;

		public ElementRowEntry(ShaderPackScreen screen, NavigationController navigation, List<AbstractElementWidget<?>> widgets) {
			super(navigation);

			this.screen = screen;
			this.widgets = widgets;
		}

		@Override
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			this.cachedWidth = listWidth;
			this.cachedPosX = x;

			// The amount of space widgets will occupy, excluding margins. Will be divided up between widgets.
			int totalWidthWithoutMargins = listWidth - (2 * (widgets.size() - 1));

			totalWidthWithoutMargins -= 3; // Centers it for some reason

			// Width of a single widget
			float singleWidgetWidth = (float) totalWidthWithoutMargins / widgets.size();

			for (int i = 0; i < widgets.size(); i++) {
				AbstractElementWidget<?> widget = widgets.get(i);
				boolean widgetHovered = isSelected && (getHoveredWidget(mouseX) == i);
				widget.render(x + (int)((singleWidgetWidth + 2) * i), y, (int) singleWidgetWidth, slotHeight + 2, mouseX, mouseY, partialTicks, widgetHovered);

				screen.setElementHoveredStatus(widget, widgetHovered);
			}
		}

		public int getHoveredWidget(int mouseX) {
			float positionAcrossWidget = ((float) MathHelper.clamp(mouseX - cachedPosX, 0, cachedWidth)) / cachedWidth;

			return MathHelper.clamp((int) Math.floor(widgets.size() * positionAcrossWidget), 0, widgets.size() - 1);
		}

		@Override
		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
			return this.mouseClicked(mouseX, mouseY, mouseEvent);
		}

		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			return this.widgets.get(getHoveredWidget((int) mouseX)).mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			this.mouseReleased(x, y, mouseEvent);
		}

		public void mouseReleased(int mouseX, int mouseY, int button) {
			this.widgets.get(getHoveredWidget((int) mouseX)).mouseReleased(mouseX, mouseY, button);
		}
	}
}
