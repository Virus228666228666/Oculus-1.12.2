package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.screen.HudHideable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiIngameForge.class)
public class MixinGuiIngameForge {

    @Inject(method = "renderGameOverlay", at = @At("HEAD"), cancellable = true)
    public void iris$handleHudHidingScreens(float partialTicks, CallbackInfo ci) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof HudHideable) {
            ci.cancel();
        }
    }

    // TODO: Move this to a more appropriate mixin
    @Inject(method = "renderGameOverlay", at = @At("RETURN"))
    public void iris$displayBigSodiumWarning(float partialTicks, CallbackInfo ci) {
        if (Iris.isSodiumInstalled()
                || Minecraft.getMinecraft().gameSettings.showDebugInfo
                || !Iris.getCurrentPack().isPresent()) {
            return;
        }

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        List<String> warningLines = new ArrayList<>();
        warningLines.add("[" + Iris.MODNAME + "] Vintagium isn't installed; you will have poor performance.");
        warningLines.add("[" + Iris.MODNAME + "] Install Vintagium if you want to run benchmarks or get higher FPS!");

        for (int i = 0; i < warningLines.size(); ++i) {
            String string = warningLines.get(i);

            final int lineHeight = 9;
            final int lineWidth = font.getStringWidth(string);
            final int y = 2 + lineHeight * i;

            Gui.drawRect(1, y - 1, 2 + lineWidth + 1, y + lineHeight - 1, 0x9050504E);
            font.drawString(string, 2, y, 0xFFFF55);
        }
    }

}
