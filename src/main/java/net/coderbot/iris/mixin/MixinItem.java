package net.coderbot.iris.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;

@Mixin(Item.class)
public class MixinItem implements IrisItemLightProvider {
}
