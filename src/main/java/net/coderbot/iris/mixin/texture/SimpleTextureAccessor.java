package net.coderbot.iris.mixin.texture;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.texture.SimpleTexture;

@Mixin(SimpleTexture.class)
public interface SimpleTextureAccessor {
	@Accessor("textureLocation")
	ResourceLocation getTextureLocation();
}
