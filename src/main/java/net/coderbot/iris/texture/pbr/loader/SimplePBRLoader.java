package net.coderbot.iris.texture.pbr.loader;

import java.io.IOException;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.coderbot.iris.mixin.texture.SimpleTextureAccessor;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;

import javax.annotation.Nullable;

public class SimplePBRLoader implements PBRTextureLoader<SimpleTexture> {
	@Override
	public void load(SimpleTexture texture, IResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		ResourceLocation location = ((SimpleTextureAccessor) texture).getTextureLocation();

		AbstractTexture normalTexture = createPBRTexture(location, resourceManager, PBRType.NORMAL);
		AbstractTexture specularTexture = createPBRTexture(location, resourceManager, PBRType.SPECULAR);

		if (normalTexture != null) {
			pbrTextureConsumer.acceptNormalTexture(normalTexture);
		}
		if (specularTexture != null) {
			pbrTextureConsumer.acceptSpecularTexture(specularTexture);
		}
	}

	@Nullable
	protected AbstractTexture createPBRTexture(ResourceLocation imageLocation, IResourceManager resourceManager, PBRType pbrType) {
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		SimpleTexture pbrTexture = new SimpleTexture(pbrImageLocation);
		try {
			pbrTexture.loadTexture(resourceManager);
		} catch (IOException e) {
			return null;
		}

		return pbrTexture;
	}
}
