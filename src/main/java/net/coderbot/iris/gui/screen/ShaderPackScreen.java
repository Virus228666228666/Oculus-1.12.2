package net.coderbot.iris.gui.screen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.ShaderPackOptionList;
import net.coderbot.iris.gui.element.ShaderPackSelectionList;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.CommentedElementWidget;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;

public class ShaderPackScreen extends GuiScreen implements HudHideable {
	/**
	 * Queue rendering to happen on top of all elements. Useful for tooltips or dialogs.
	 */
	public static final Set<Runnable> TOP_LAYER_RENDER_QUEUE = new HashSet<>();

	private static final ITextComponent SELECT_TITLE = new TextComponentTranslation("pack.iris.select.title").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
	private static final ITextComponent CONFIGURE_TITLE = new TextComponentTranslation("pack.iris.configure.title").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
	private static final ITextComponent title = new TextComponentTranslation("options.iris.shaderPackSelection.title");
	private static final int COMMENT_PANEL_WIDTH = 314;

	private final GuiScreen parent;
	private final List<GuiSlot> children;
	private final ITextComponent irisTextComponent;

	private ShaderPackSelectionList shaderPackList;

	private @Nullable ShaderPackOptionList shaderOptionList = null;
	private @Nullable NavigationController navigation = null;
	private GuiButton screenSwitchButton;

	private ITextComponent notificationDialog = null;
	private int notificationDialogTimer = 0;

	private @Nullable AbstractElementWidget<?> hoveredElement = null;
	private Optional<ITextComponent> hoveredElementCommentTitle = Optional.empty();
	private List<String> hoveredElementCommentBody = new ArrayList<>();
	private int hoveredElementCommentTimer = 0;

	private boolean optionMenuOpen = false;

	private boolean dropChanges = false;
	private static final String development = "Development Environment";
	private ITextComponent developmentComponent;
	private ITextComponent updateComponent;

	private boolean guiHidden = false;
	private float guiButtonHoverTimer = 0.0f;

	private static final int DONE_BUTTON_ID = 0;
	private static final int APPLY_BUTTON_ID = 1;
	private static final int DROP_BUTTON_ID = 2;
	private static final int OPEN_BUTTON_ID = 3;
	private static final int LIST_BUTTON_ID = 4;
	private static final int GUI_BUTTON_ID = 5;

	public ShaderPackScreen(GuiScreen parent) {
		super();
		this.parent = parent;
		this.children = new ArrayList<GuiSlot>();

		String irisName = Iris.MODNAME + " " + Iris.getVersion();

		if (irisName.contains("-development-environment")) {
			this.developmentComponent = new TextComponentString(TextFormatting.GOLD + development);
			irisName = irisName.replace("-development-environment", "");
		}

		this.irisTextComponent = new TextComponentString(irisName).setStyle(new Style().setColor(TextFormatting.GRAY));

		refreshForChangedPack();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.mc.world == null) {
			this.drawDefaultBackground();
		} else if (!this.guiHidden) {
			this.drawGradientRect(0, 0, width, height, 0x4F232323, 0x4F232323);
		}

		if (!this.guiHidden) {
			if (optionMenuOpen && this.shaderOptionList != null) {
				this.shaderOptionList.drawScreen(mouseX, mouseY, partialTicks);
			} else {
				this.shaderPackList.drawScreen(mouseX, mouseY, partialTicks);
			}
		}

		float previousHoverTimer = this.guiButtonHoverTimer;
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (previousHoverTimer == this.guiButtonHoverTimer) {
			this.guiButtonHoverTimer = 0.0f;
		}

		if (!this.guiHidden) {
			drawCenteredString(this.fontRenderer, title.getFormattedText(), (int) (this.width * 0.5), 8, 0xFFFFFF);

			if (notificationDialog != null && notificationDialogTimer > 0) {
				drawCenteredString(this.fontRenderer, notificationDialog.getFormattedText(), (int) (this.width * 0.5), 21, 0xFFFFFF);
			} else {
				if (optionMenuOpen) {
					drawCenteredString(this.fontRenderer, CONFIGURE_TITLE.getFormattedText(), (int) (this.width * 0.5), 21, 0xFFFFFF);
				} else {
					drawCenteredString(this.fontRenderer, SELECT_TITLE.getFormattedText(), (int) (this.width * 0.5), 21, 0xFFFFFF);
				}
			}

			// Draw the comment panel
			if (this.isDisplayingComment()) {
				// Determine panel height and position
				int panelHeight = Math.max(50, 18 + (this.hoveredElementCommentBody.size() * 10));
				int x = (int) (0.5 * this.width) - 157;
				int y = this.height - (panelHeight + 4);
				// Draw panel
				GuiUtil.drawPanel(x, y, COMMENT_PANEL_WIDTH, panelHeight);
				// Draw text
				this.fontRenderer.drawStringWithShadow(this.hoveredElementCommentTitle.toString(), x + 4, y + 4, 0xFFFFFF);
				for (int i = 0; i < this.hoveredElementCommentBody.size(); i++) {
					this.fontRenderer.drawStringWithShadow(String.valueOf(this.hoveredElementCommentBody.toString().charAt(i)), x + 4, (y + 16) + (i * 10), 0xFFFFFF);
				}
			}
		}

		// Render everything queued to render last
		for (Runnable render : TOP_LAYER_RENDER_QUEUE) {
			render.run();
		}
		TOP_LAYER_RENDER_QUEUE.clear();

		if (this.developmentComponent != null) {
			this.fontRenderer.drawStringWithShadow(developmentComponent.getFormattedText(), 2, this.height - 10, 0xFFFFFF);
			this.fontRenderer.drawStringWithShadow(irisTextComponent.getFormattedText(), 2, this.height - 20, 0xFFFFFF);
		} else if (this.updateComponent != null) {
			this.fontRenderer.drawStringWithShadow(updateComponent.getFormattedText(), 2, this.height - 10, 0xFFFFFF);
			this.fontRenderer.drawStringWithShadow(irisTextComponent.getFormattedText(), 2, this.height - 20, 0xFFFFFF);
		} else {
			this.fontRenderer.drawStringWithShadow(irisTextComponent.getFormattedText(), 2, this.height - 10, 0xFFFFFF);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int bottomCenter = this.width / 2 - 50;
		int topCenter = this.width / 2 - 76;
		boolean inWorld = this.mc.world != null;

		this.children.remove(this.shaderPackList);
		this.children.remove(this.shaderOptionList);

		this.shaderPackList = new ShaderPackSelectionList(this, this.mc, this.width, this.height, 32, this.height - 58, 0, this.width);

		if (Iris.getCurrentPack().isPresent() && this.navigation != null) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.shaderOptionList = new ShaderPackOptionList(this, this.navigation, currentPack, this.mc, this.width, this.height, 32, this.height - 58, 0, this.width);
			this.navigation.setActiveOptionList(this.shaderOptionList);

			this.shaderOptionList.rebuild();
		} else {
			optionMenuOpen = false;
			this.shaderOptionList = null;
		}

		if (inWorld) {
			// todo
			this.shaderPackList.setRenderBackground(false);
			if (shaderOptionList != null) {
				this.shaderOptionList.setRenderBackground(false);
			}
		}

		this.buttonList.clear();
		this.children.clear();

		if (!this.guiHidden) {
			if (optionMenuOpen && shaderOptionList != null) {
				this.children.add(shaderOptionList);
			} else {
				this.children.add(shaderPackList);
			}

			this.addButton(new GuiButton(DONE_BUTTON_ID, bottomCenter + 104, this.height - 27, 100, 20,
					new TextComponentTranslation("options.iris.done").getFormattedText()));

			this.addButton(new GuiButton(APPLY_BUTTON_ID, bottomCenter, this.height - 27, 100, 20,
					new TextComponentTranslation("options.iris.apply").getFormattedText()));

			this.addButton(new GuiButton(DROP_BUTTON_ID, bottomCenter - 104, this.height - 27, 100, 20,
					new TextComponentTranslation("options.iris.cancel").getFormattedText()));

			this.addButton(new GuiButton(OPEN_BUTTON_ID, topCenter - 78, this.height - 51, 152, 20,
					new TextComponentTranslation("options.iris.openShaderPackFolder").getFormattedText()));

			this.addButton(new GuiButton(LIST_BUTTON_ID, topCenter + 78, this.height - 51, 152, 20,
					new TextComponentTranslation("options.iris.shaderPackList").getFormattedText()));
			this.screenSwitchButton = this.buttonList.get(LIST_BUTTON_ID);

			refreshScreenSwitchButton();
		}

		if (inWorld) {
			ITextComponent showOrHide = this.guiHidden
				? new TextComponentTranslation("options.iris.gui.show")
				: new TextComponentTranslation("options.iris.gui.hide");

			float endOfLastButton = this.width / 2.0f + 154.0f;
			float freeSpace = this.width - endOfLastButton;
			int x;
			if (freeSpace > 100.0f) {
				x = this.width - 50;
			} else if (freeSpace < 20.0f) {
				x = this.width - 20;
			} else {
				x = (int) (endOfLastButton + (freeSpace / 2.0f)) - 10;
			}

			// this button toggles the GUI when in world, to demonstrate shader effects
			GuiButton GUI_BUTTON = new GuiButtonImage(
					GUI_BUTTON_ID, x, this.height - 39, 20, 20,
					this.guiHidden ? 20 : 0, 146, 146,
					GuiUtil.IRIS_WIDGETS_TEX
			);
			GUI_BUTTON.displayString = showOrHide.getFormattedText();
			this.addButton(GUI_BUTTON);
		}

		// NB: Don't let comment remain when exiting options screen
		// https://github.com/IrisShaders/Iris/issues/1494
		this.hoveredElement = null;
		this.hoveredElementCommentTimer = 0;
	}

	public void refreshForChangedPack() {
		if (Iris.getCurrentPack().isPresent()) {
			ShaderPack currentPack = Iris.getCurrentPack().get();

			this.navigation = new NavigationController(currentPack.getMenuContainer());

			if (this.shaderOptionList != null) {
				this.shaderOptionList.applyShaderPack(currentPack);
				this.shaderOptionList.rebuild();
			}
		} else {
			this.navigation = null;
		}

		refreshScreenSwitchButton();
	}

	public void refreshScreenSwitchButton() {
		if (this.screenSwitchButton != null) {
			this.screenSwitchButton.displayString = optionMenuOpen
					? I18n.format("options.iris.shaderPackList")
					: I18n.format("options.iris.shaderPackSettings");
			this.screenSwitchButton.enabled = optionMenuOpen || this.shaderPackList.getTopButtonRow().shadersEnabled;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if (this.notificationDialogTimer > 0) {
			this.notificationDialogTimer--;
		}

		if (this.hoveredElement != null) {
			this.hoveredElementCommentTimer++;
		} else {
			this.hoveredElementCommentTimer = 0;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

        if (typedChar != 'a') {
            if (keyCode == 1) {
                if (this.guiHidden) {
                    this.guiHidden = false;
                    this.initGui();
                } else if (this.navigation != null && this.navigation.hasHistory()) {
                    this.navigation.back();
                } else if (this.optionMenuOpen) {
                    this.optionMenuOpen = false;
                    this.initGui();
                }
            }
        }
    }

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (int i = 0; i < this.buttonList.size(); ++i) {
			GuiButton guibutton = this.buttonList.get(i);
			if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
				if (guibutton.id == DONE_BUTTON_ID) {
					this.mc.displayGuiScreen(parent);
				} else if (guibutton.id == APPLY_BUTTON_ID) {
					this.applyChanges();
				} else if (guibutton.id == DROP_BUTTON_ID) {
					this.dropChangesAndClose();
				} else if (guibutton.id == OPEN_BUTTON_ID) {
					this.openShaderPackFolder();
				} else if (guibutton.id == LIST_BUTTON_ID) {
					this.optionMenuOpen = !this.optionMenuOpen;

					// UX: Apply changes before switching screens to avoid unintuitive behavior
					//
					// Not doing this leads to unintuitive behavior, since selecting a pack in the
					// list (but not applying) would open the settings for the previous pack, rather
					// than opening the settings for the selected (but not applied) pack.
					this.applyChanges();

					this.initGui();
				} else if (guibutton.id == GUI_BUTTON_ID) {
					if (guibutton.isMouseOver()) {
						this.guiHidden = !this.guiHidden;
					}
					this.initGui();
				} else {
					throw new RuntimeException("Button not defined: " + guibutton.id);
				}
				return;
			}
		}

		if (!this.guiHidden) {
			if (optionMenuOpen && this.shaderOptionList != null) {
				this.shaderOptionList.mouseClicked(mouseX, mouseY, mouseButton);
				if (this.hoveredElement != null) {
					this.hoveredElement.mouseClicked(mouseX, mouseY, mouseButton);
					this.initGui();
				}
			} else {
				this.shaderPackList.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (optionMenuOpen && this.shaderOptionList != null) {
			this.shaderOptionList.mouseReleased(mouseX, mouseY, state);
		} else {
			this.shaderPackList.mouseReleased(mouseX, mouseY, state);
		}
	}

	public void displayNotification(ITextComponent component) {
		this.notificationDialog = component;
		this.notificationDialogTimer = 100;
	}

	public void importPackOptions(Path settingFile) {
		try (InputStream in = Files.newInputStream(settingFile)) {
			Properties properties = new Properties();
			properties.load(in);

			Iris.queueShaderPackOptionsFromProperties(properties);

			this.notificationDialog = new TextComponentTranslation(
					"options.iris.shaderPackOptions.importedSettings",
					settingFile.getFileName().toString()
			).setStyle(new Style().setColor(TextFormatting.YELLOW).setItalic(true));
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)

			if (this.navigation != null) {
				this.navigation.refresh();
			}
		} catch (Exception e) {
			// If the file could not be properly parsed or loaded,
			// log the error and display a message to the user
			Iris.logger.error("Error importing shader settings file \""+ settingFile.toString() +"\"", e);

			this.notificationDialog = new TextComponentTranslation(
					"options.iris.shaderPackOptions.failedImport",
					settingFile.getFileName().toString()
			).setStyle(new Style().setColor(TextFormatting.RED).setItalic(true));
			this.notificationDialogTimer = 100; // 5 seconds (100 ticks)
		}
	}

	@Override
	public void onGuiClosed() {
		if (!dropChanges) {
			applyChanges();
		} else {
			discardChanges();
		}
	}

	private void dropChangesAndClose() {
		dropChanges = true;
		this.mc.displayGuiScreen(parent);
	}

	public void applyChanges() {
		ShaderPackSelectionList.BaseEntry base = this.shaderPackList.getSelected();

		if (!(base instanceof ShaderPackSelectionList.ShaderPackEntry)) {
			return;
		}

		ShaderPackSelectionList.ShaderPackEntry entry = (ShaderPackSelectionList.ShaderPackEntry)base;
		this.shaderPackList.setApplied(entry);

		String name = entry.getPackName();

		// If the pack is being changed, clear pending options from the previous pack to
		// avoid possible undefined behavior from applying one pack's options to another pack
		if (!name.equals(Iris.getCurrentPackName())) {
			Iris.clearShaderPackOptionQueue();
		}

		boolean enabled = this.shaderPackList.getTopButtonRow().shadersEnabled;

		String previousPackName = Iris.getIrisConfig().getShaderPackName().orElse(null);
		boolean previousShadersEnabled = Iris.getIrisConfig().areShadersEnabled();

		// Only reload if the pack would be different from before, or shaders were toggled, or options were changed, or if we're about to reset options.
		if (!name.equals(previousPackName) || enabled != previousShadersEnabled || !Iris.getShaderPackOptionQueue().isEmpty() || Iris.shouldResetShaderPackOptionsOnNextReload()) {
			Iris.getIrisConfig().setShaderPackName(name);
			IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);
		}

		refreshForChangedPack();
	}

	private void discardChanges() {
		Iris.clearShaderPackOptionQueue();
	}

	private void openShaderPackFolder() {
		CompletableFuture.runAsync(() -> open(Iris.getShaderpacksDirectoryManager().getDirectoryUri().toString()));
	}

	private static String[] getURLOpenCommand(String url) {
		switch (Util.getOSType()) {
			case WINDOWS:
				return new String[]{"rundll32", "url.dll,FileProtocolHandler", url};
			case OSX:
				return new String[]{"open", url};
			case UNKNOWN:
			case LINUX:
			case SOLARIS:
				return new String[]{"xdg-open", url};
			default:
				throw new IllegalArgumentException("Unexpected OS Type");
		}
	}

	public static void open(String url) {
		try {
			Runtime.getRuntime().exec(getURLOpenCommand(url));
		} catch (IOException exception) {
			Iris.logger.error("Couldn't open url '{}'", url, exception);
		}
	}

	// Let the screen know if an element is hovered or not, allowing for accurately updating which element is hovered
	public void setElementHoveredStatus(AbstractElementWidget<?> widget, boolean hovered) {
		if (hovered && widget != this.hoveredElement) {
			this.hoveredElement = widget;

			if (widget instanceof CommentedElementWidget) {
				this.hoveredElementCommentTitle = ((CommentedElementWidget<?>) widget).getCommentTitle();

				Optional<ITextComponent> commentBody = ((CommentedElementWidget<?>) widget).getCommentBody();
				if (!commentBody.isPresent()) {
					this.hoveredElementCommentBody.clear();
				} else {
					String rawCommentBody = commentBody.get().getFormattedText();

					// Strip any trailing "."s
					if (rawCommentBody.endsWith(".")) {
						rawCommentBody = rawCommentBody.substring(0, rawCommentBody.length() - 1);
					}
					// Split comment body into lines by separator ". "
					List<ITextComponent> splitByPeriods = Arrays.stream(rawCommentBody.split("\\. [ ]*")).map(TextComponentString::new).collect(Collectors.toList());
					// Line wrap
					this.hoveredElementCommentBody = new ArrayList<>();
					for (ITextComponent text : splitByPeriods) {
						this.hoveredElementCommentBody.addAll(this.fontRenderer.listFormattedStringToWidth(text.getFormattedText(), COMMENT_PANEL_WIDTH - 8));
					}
				}
			} else {
				this.hoveredElementCommentTitle = Optional.empty();
				this.hoveredElementCommentBody.clear();
			}

			this.hoveredElementCommentTimer = 0;
		} else if (!hovered && widget == this.hoveredElement) {
			this.hoveredElement = null;
			this.hoveredElementCommentTitle = Optional.empty();
			this.hoveredElementCommentBody.clear();
			this.hoveredElementCommentTimer = 0;
		}
	}

	public boolean isDisplayingComment() {
		return this.hoveredElementCommentTimer > 20 &&
				this.hoveredElementCommentTitle.isPresent() &&
				!this.hoveredElementCommentBody.isEmpty();
	}
}
