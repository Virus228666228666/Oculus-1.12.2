package net.coderbot.iris.compat.sodium.mixin.directional_shading;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import me.jellysquid.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;

@Mixin(FlatLightPipeline.class)
public class MixinFlatLightPipeline {
	// todo: unable to locate obfuscation mapping
//	@Redirect(method = "calculate", at = @At(value = "INVOKE",
//			target = "Lme/jellysquid/mods/sodium/client/world/WorldSlice;getBrightness(Lnet/minecraft/util/EnumFacing;Z)F"))
//	private float iris$getBrightness(WorldSlice level, EnumFacing direction, boolean shaded) {
//		if (BlockRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
//			return 1.0F;
//		} else {
//			return level.getBrightness(direction, shaded);
//		}
//	}
}
