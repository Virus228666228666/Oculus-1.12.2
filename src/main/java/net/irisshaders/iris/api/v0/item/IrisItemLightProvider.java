package net.irisshaders.iris.api.v0.item;

import net.coderbot.iris.vendored.joml.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public interface IrisItemLightProvider {

	Vector3f DEFAULT_LIGHT_COLOR = new Vector3f(1, 1, 1);

	default int getLightEmission(EntityPlayerSP player, ItemStack stack) {
		if (stack.getItem() instanceof ItemBlock) {
			ItemBlock item = (ItemBlock) stack.getItem();
			Block block = item.getBlock();

			return block.getDefaultState().getLightValue();
		}

		return 0;
	}

	default Vector3f getLightColor(EntityPlayerSP player, ItemStack stack) {
		return DEFAULT_LIGHT_COLOR;
	}
}
