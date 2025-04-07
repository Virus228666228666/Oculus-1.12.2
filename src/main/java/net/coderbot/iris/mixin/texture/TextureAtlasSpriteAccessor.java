package net.coderbot.iris.mixin.texture;

import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
public interface TextureAtlasSpriteAccessor {
	@Accessor("animationMetadata")
	AnimationMetadataSection getMetadata();

	@Accessor("originX")
	int getOriginX();

	@Accessor("originY")
	int getOriginY();
}
