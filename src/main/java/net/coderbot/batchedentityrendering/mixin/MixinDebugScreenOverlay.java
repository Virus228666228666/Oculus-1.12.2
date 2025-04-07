package net.coderbot.batchedentityrendering.mixin;

import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.coderbot.batchedentityrendering.impl.BatchingDebugMessageHelper;
import net.coderbot.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;

/**
 * Adds entity batching debug information to the debug screen. Uses a priority of 1010
 * so that we apply after other mixins to the debug screen (such as the one that adds Iris
 * shader pack information), so that the entity batching debug logic appears at the bottom.
 */
@Mixin(value = GuiOverlayDebug.class, priority = 1010)
public abstract class MixinDebugScreenOverlay {
    @Inject(method = "renderDebugInfoLeft", at = @At("RETURN"))
    private void batchedentityrendering$renderDebugInfoLeft(CallbackInfo ci) {
        List<String> messages = Collections.singletonList(Minecraft.getMinecraft().debug);
        DrawCallTrackingRenderBuffers drawTracker = (DrawCallTrackingRenderBuffers) Minecraft.getMinecraft().renderGlobal;

        // blank line separator
        messages.add("");
        messages.add("[Entity Batching] " + BatchingDebugMessageHelper.getDebugMessage(drawTracker));
    }
}
