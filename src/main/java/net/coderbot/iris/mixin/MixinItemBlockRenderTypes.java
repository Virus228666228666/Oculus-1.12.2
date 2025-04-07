package net.coderbot.iris.mixin;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.block.Block;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Block.class)
public class MixinItemBlockRenderTypes {
	@Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
	private void iris$setCustomRenderType(CallbackInfoReturnable<BlockRenderLayer> cir) {
		Map<IRegistryDelegate<Block>, BlockRenderLayer> idMap = BlockRenderingSettings.INSTANCE.getBlockTypeIds();
		if (idMap != null) {
			BlockRenderLayer type = idMap.get(((Block) (Object) this).delegate);
			if (type != null) {
				cir.setReturnValue(type);
			}
		}
	}
}
