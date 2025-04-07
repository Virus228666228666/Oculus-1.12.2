package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.mixin.statelisteners.BooleanStateAccessor;
import net.minecraft.client.renderer.GlStateManager;

public class FogUniforms {
	private FogUniforms() {
		// no construction
	}

	public static void addFogUniforms(DynamicUniformHolder uniforms) {
		uniforms.uniform1i("fogMode", () -> {
			GlStateManager.FogState fog = GlStateManagerAccessor.getFOG();

			if (!((BooleanStateAccessor) fog.fog).isEnabled()) {
				return 0;
			}

			return GlStateManagerAccessor.getFOG().mode;
		}, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogModeNotifier.setListener(listener);
		});

		uniforms.uniform1f("fogDensity", () -> GlStateManagerAccessor.getFOG().density, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogDensityNotifier.setListener(listener);
		});

		uniforms.uniform1f("fogStart", () -> GlStateManagerAccessor.getFOG().start, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogStartNotifier.setListener(listener);
		});

		uniforms.uniform1f("fogEnd", () -> GlStateManagerAccessor.getFOG().end, listener -> {
			StateUpdateNotifiers.fogToggleNotifier.setListener(listener);
			StateUpdateNotifiers.fogEndNotifier.setListener(listener);
		});
	}
}
