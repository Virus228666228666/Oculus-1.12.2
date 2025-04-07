package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.gui.option.ShaderPackSelectionButtonOption;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiVideoSettings.class)
public abstract class MixinVideoSettingsScreen extends GuiScreen {
	@Unique
	private final GameSettings settings;

	public MixinVideoSettingsScreen(GameSettings settings) {
		this.settings = settings;
	}

	@Inject(method = "initGui", at = @At("RETURN"))
	private void iris$addShaderPackScreenButton(CallbackInfo ci) {
		// Add our custom buttons after the original initGui has executed
		this.buttonList.add(new ShaderPackSelectionButtonOption(this, this.mc));
		this.buttonList.add(IrisVideoSettings.RENDER_DISTANCE.createButton(this.mc.gameSettings, this.width / 2 - 155, this.height / 6 + 24 * (this.buttonList.size() / 2), 150));
	}
}