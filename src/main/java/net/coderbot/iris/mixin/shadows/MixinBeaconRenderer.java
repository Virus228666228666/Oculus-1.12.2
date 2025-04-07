package net.coderbot.iris.mixin.shadows;

import net.coderbot.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TileEntityBeaconRenderer.class)
public class MixinBeaconRenderer {
	@Inject(method = "renderBeacon",
	        at = @At("HEAD"), cancellable = true)
	private void iris$noLightBeamInShadowPass(double x, double y, double z, double partialTicks, double textureScale, List<TileEntityBeacon.BeamSegment> beamSegments, double totalWorldTime, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// TODO: Don't do this if we're doing the "Unified Entity Rendering" optimization
			// TODO: This isn't necessary on most shaderpacks if we support blockEntityId
			ci.cancel();
		}
	}
}
