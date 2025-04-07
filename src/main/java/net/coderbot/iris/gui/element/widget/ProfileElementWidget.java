package net.coderbot.iris.gui.element.widget;

import java.util.Optional;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.ProfileSet;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuProfileElement;
import net.coderbot.iris.shaderpack.option.values.OptionValues;
import net.minecraft.util.text.*;
import net.minecraft.client.Minecraft;


public class ProfileElementWidget extends BaseOptionElementWidget<OptionMenuProfileElement> {
	private static final ITextComponent PROFILE_LABEL = new TextComponentTranslation("options.iris.profile");
	private static final ITextComponent PROFILE_CUSTOM = new TextComponentTranslation("options.iris.profile.custom").setStyle(new Style().setColor(TextFormatting.YELLOW));

	private Profile next;
	private Profile previous;
	private ITextComponent profileLabel;

	public ProfileElementWidget(OptionMenuProfileElement element) {
		super(element);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		super.init(screen, navigation);
		this.setLabel(PROFILE_LABEL);

		ProfileSet profiles = this.element.profiles;
		OptionSet options = this.element.options;
		OptionValues pendingValues = this.element.getPendingOptionValues();

		ProfileSet.ProfileResult result = profiles.scan(options, pendingValues);

		this.next = result.next;
		this.previous = result.previous;
		Optional<String> profileName = result.current.map(p -> p.name);

		this.profileLabel = profileName.map(name -> GuiUtil.translateOrDefault(new TextComponentString(name), "profile." + name)).orElse(PROFILE_CUSTOM);
	}

	@Override
	public void render(int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, width - (Minecraft.getMinecraft().fontRenderer.getStringWidth(PROFILE_LABEL.getFormattedText()) + 16));

		this.renderOptionWithValue(x, y, width, height, hovered);
	}

	@Override
	protected ITextComponent createValueLabel() {
		return this.profileLabel;
	}

	@Override
	public Optional<ITextComponent> getCommentTitle() {
		return Optional.of(PROFILE_LABEL);
	}

	@Override
	public String getCommentKey() {
		return "profile.comment";
	}

	@Override
	public boolean applyNextValue() {
		if (this.next == null) {
			return false;
		}

		Iris.queueShaderPackOptionsFromProfile(this.next);
		return true;
	}

	@Override
	public boolean applyPreviousValue() {
		if (this.previous == null) {
			return false;
		}

		Iris.queueShaderPackOptionsFromProfile(this.previous);

		return true;
	}

	@Override
	public boolean applyOriginalValue() {
		return false; // Resetting options is the way to return to the "default profile"
	}

	@Override
	public boolean isValueModified() {
		return false;
	}

	@Override
	public boolean mouseClicked(int mx, int my, int button) {
		if (button == 1) {
			return applyPreviousValue();
		} else {
			return applyNextValue();
		}
	}
}
