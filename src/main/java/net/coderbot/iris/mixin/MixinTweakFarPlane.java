package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tweaks the far plane of the projection matrix to match OptiFine.
 *
 * As it turns out, OptiFine significantly reduces the far plane distance compared to vanilla. This is likely because
 * vanilla chooses a far plane that is four times the render distance in blocks, which is a bit overkill. Instead,
 * OptiFine makes the far plane only two times the render distance in blocks, and to compensate, adds a minimum distance
 * for the far plane to make it not appear too closely to the player.
 *
 * OptiFine also modifies the distance of the far plane based on the fog setting. We mimic the "fast" fog setting
 * because that is the setting that reduces the far plane distance the most.
 *
 * So why is this needed? As it turns out, shaderpacks actually rely on this behavior to work properly. Most notably,
 * the water reflection code in Sildur's Vibrant Shaders will often create impossible reflections with the default far
 * plane, where things that are close to the player will be reflected in water that is very far away.
 *
 * A possible reason for this is that the value of the {@code far} uniform does not actually match the far plane
 * distance, so shaderpacks one way or another have come to just bodge things to work around the issue, and in the
 * process become subtly reliant on OptiFine implementation details.
 *
 * Fun.
 */
// TODO Partially was made from Optifine patches, partially from Iris. NOT SURE
@Mixin(EntityRenderer.class)
public class MixinTweakFarPlane {
	@Shadow
	private float farPlaneDistance;

	@Unique
	private float clipDistanceSqrt = 128.0F;
	@Unique
	private float clipDistanceTwo = 128.0F;

	@Inject(method = "setupCameraTransform", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F", shift = At.Shift.AFTER))
	private void iris$tweakViewDistanceBasedOnFog(float partialTicks, int pass, CallbackInfo ci) {
		if (!Iris.getCurrentPack().isPresent()) {
			// Don't mess with the far plane if no shaderpack is loaded
			return;
		}

		// Tweak the view distance based on the fog setting
		//
		// Coefficient values: 0.83 for fast fog, 0.95 for fancy fog, 1.0 for no fog
		//
		// On 1.16, we select the value based on if GL_NV_fog_distance is supported, and on 1.17+ only fancy fog is supported.

		farPlaneDistance *= GLContext.getCapabilities().GL_NV_fog_distance ? 0.95F : 0.83F;



		if (!Iris.getCurrentPack().isPresent()) {
			// Don't mess with the far plane if no shaderpack is loaded
			this.clipDistanceSqrt = this.farPlaneDistance * MathHelper.SQRT_2;
			this.clipDistanceTwo = this.farPlaneDistance * 2.0F;
			return;
		}

		float tweakedViewDistance = this.farPlaneDistance;

		// Halve the distance of the far plane in the projection matrix from vanilla. Normally, the far plane is MathHelper.SQRT_2 times
		// the view distance, but this makes it so that it is only two times the view distance.
		tweakedViewDistance *= 0.5;

		// Use a minimum distance for the far plane
		// The real far plane will be MathHelper.SQRT_2 times this, so this will result in a far plane of 173 meters.
		//
		// Math.max returns the maximum of the two values, so whenever tweakedViewDistance falls below 122.3294731455404F, this code
		// forces it to take on a value of 122.3294731455404F.
		this.clipDistanceSqrt = Math.max(122.3294731455404F, tweakedViewDistance);
		this.clipDistanceTwo = Math.max(86.5F, tweakedViewDistance);
	}

	@Redirect(method = "setupCameraTransform", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F", ordinal = 1))
	private float iris$tweakViewDistanceToMatchOptiFine(EntityRenderer instance) {
		return this.clipDistanceSqrt;
	}

	@Redirect(method = "renderHand", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F"))
	private float iris$tweakViewDistanceToMatchOptiFineHand(EntityRenderer instance) {
		return this.clipDistanceTwo;
	}

	@Redirect(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F", ordinal = 0))
	private float iris$tweakViewDistanceToMatchOptiFineWorld1(EntityRenderer instance) {
		return this.clipDistanceTwo;
	}

	@Redirect(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F", ordinal = 1))
	private float iris$tweakViewDistanceToMatchOptiFineWorld2(EntityRenderer instance) {
		return this.clipDistanceSqrt;
	}

	@Redirect(method = "renderCloudsCheck", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F", ordinal = 0))
	private float iris$tweakViewDistanceToMatchOptiFineClouds1(EntityRenderer instance) {
		return this.clipDistanceTwo * 2;
	}

	@Redirect(method = "renderCloudsCheck", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;farPlaneDistance:F", ordinal = 1))
	private float iris$tweakViewDistanceToMatchOptiFineClouds2(EntityRenderer instance) {
		return this.clipDistanceSqrt;
	}
}
