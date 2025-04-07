package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DynamicTexture.class)
public interface DynamicTextureAccessor {
    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();
}
