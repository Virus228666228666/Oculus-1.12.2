package net.coderbot.iris.compat.sodium.mixin.options;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;

import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import net.coderbot.iris.gui.screen.ShaderPackScreen;

/**
 * Adds our Shader Packs button to the Sodium options GUI.
 */
@Mixin(SodiumOptionsGUI.class)
public class MixinSodiumOptionsGUI extends GuiScreen {
	@Shadow(remap = false)
	@Final
	private List<OptionPage> pages;

	@Shadow(remap = false)
	private OptionPage currentPage;

	@Unique
	private OptionPage shaderPacks;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$onInit(GuiScreen prevScreen, CallbackInfo ci) {
		shaderPacks = new OptionPage(new TextComponentTranslation("options.iris.shaderPackSelection"), ImmutableList.of());
		pages.add(shaderPacks);
	}

	@Inject(method = "setPage", at = @At("HEAD"), remap = false, cancellable = true)
	private void iris$onSetPage(OptionPage page, CallbackInfo ci) {
		if (page == shaderPacks) {
			mc.displayGuiScreen(new ShaderPackScreen(this));
			ci.cancel();
		}
	}
}
