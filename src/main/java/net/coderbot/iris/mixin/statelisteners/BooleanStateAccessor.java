package net.coderbot.iris.mixin.statelisteners;

import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.BooleanState.class)
public interface BooleanStateAccessor {
	@Accessor("currentState")
	boolean isEnabled();
}
