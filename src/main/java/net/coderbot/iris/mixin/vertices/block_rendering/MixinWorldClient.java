package net.coderbot.iris.mixin.vertices.block_rendering;

import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;

/**
 * Allows the vanilla directional shading effect to be fully disabled by shader packs. This is needed by many packs
 * because they implement their own lighting effects, which visually clash with vanilla's directional shading lighting.
 */
@Mixin(WorldClient.class)
public class MixinWorldClient {
	// there is no getShade method in 1.12.2
	// possibly FaceBakery.getFaceShadeColor? unusure
	// todo
//	@ModifyVariable(method = "getShade", at = @At("HEAD"), argsOnly = true)
//	private boolean iris$maybeDisableDirectionalShading(boolean shaded) {
//		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
//			return false;
//		} else {
//			return shaded;
//		}
//	}
}
