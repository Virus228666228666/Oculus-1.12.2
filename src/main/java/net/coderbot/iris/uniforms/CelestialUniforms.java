package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.util.Objects;

import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.vendored.joml.Quaternionf;
import net.coderbot.iris.vendored.joml.Vector3f;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Matrix4f;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#celestial-bodies">Uniforms: Celestial bodies</a>
 */
public final class CelestialUniforms {
	private final float sunPathRotation;

	public CelestialUniforms(float sunPathRotation) {
		this.sunPathRotation = sunPathRotation;
	}

	public void addCelestialUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(PER_FRAME, "sunAngle", CelestialUniforms::getSunAngle)
			.uniformTruncated3f(PER_FRAME, "sunPosition", this::getSunPosition)
			.uniformTruncated3f(PER_FRAME, "moonPosition", this::getMoonPosition)
			.uniform1f(PER_FRAME, "shadowAngle", CelestialUniforms::getShadowAngle)
			.uniformTruncated3f(PER_FRAME, "shadowLightPosition", this::getShadowLightPosition)
			.uniformTruncated3f(PER_FRAME, "upPosition", CelestialUniforms::getUpPosition);
	}

	public static float getSunAngle() {
		float skyAngle = getSkyAngle();

		if (skyAngle < 0.75F) {
			return skyAngle + 0.25F;
		} else {
			return skyAngle - 0.75F;
		}
	}

	private static float getShadowAngle() {
		float shadowAngle = getSunAngle();

		if (!isDay()) {
			shadowAngle -= 0.5F;
		}

		return shadowAngle;
	}

	private Vector4f getSunPosition() {
		return getCelestialPosition(100.0F);
	}

	private Vector4f getMoonPosition() {
		return getCelestialPosition(-100.0F);
	}

	public Vector4f getShadowLightPosition() {
		return isDay() ? getSunPosition() : getMoonPosition();
	}

	public Vector4f getShadowLightPositionInWorldSpace() {
		return isDay() ? getCelestialPositionInWorldSpace(100.0F) : getCelestialPositionInWorldSpace(-100.0F);
	}

	private Vector4f getCelestialPositionInWorldSpace(float y) {
		Vector4f position = new Vector4f(0.0F, y, 0.0F, 0.0F);

		// TODO: Deduplicate / remove this function.
		Matrix4f celestial = new Matrix4f();
		celestial.setIdentity();

		// This is the same transformation applied by renderSky, however, it's been moved to here.
		// This is because we need the result of it before it's actually performed in vanilla.
		celestial.rotate(-90.0F, new org.lwjgl.util.vector.Vector3f(0.0F, 1.0F, 0.0F));
		celestial.rotate(sunPathRotation, new org.lwjgl.util.vector.Vector3f(0.0F, 0.0F, 1.0F));
		celestial.rotate(getSkyAngle() * 360.0F, new org.lwjgl.util.vector.Vector3f(1.0F, 0.0F, 0.0F));

		float f = position.x;
		float f1 = position.y;
		float f2 = position.z;
		float f3 = position.w;
		position.x = celestial.m00 * f + celestial.m01 * f1 + celestial.m02 * f2 + celestial.m03 * f3;
		position.y = celestial.m10 * f + celestial.m11 * f1 + celestial.m12 * f2 + celestial.m13 * f3;
		position.z = celestial.m20 * f + celestial.m21 * f1 + celestial.m22 * f2 + celestial.m23 * f3;
		position.w = celestial.m30 * f + celestial.m31 * f1 + celestial.m32 * f2 + celestial.m33 * f3;

		return JomlConversions.toJoml(position);
	}

	private Vector4f getCelestialPosition(float y) {
		Vector4f position = new Vector4f(0.0F, y, 0.0F, 0.0F);

		Matrix4f celestial = CapturedRenderingState.INSTANCE.getGbufferModelView();

		// This is the same transformation applied by renderSky, however, it's been moved to here.
		// This is because we need the result of it before it's actually performed in vanilla.
		celestial.rotate(-90.0F, new org.lwjgl.util.vector.Vector3f(0.0F, 1.0F, 0.0F));
		celestial.rotate(sunPathRotation, new org.lwjgl.util.vector.Vector3f(0.0F, 0.0F, 1.0F));
		celestial.rotate(getSkyAngle() * 360.0F, new org.lwjgl.util.vector.Vector3f(1.0F, 0.0F, 0.0F));

		float f = position.x;
		float f1 = position.y;
		float f2 = position.z;
		float f3 = position.w;
		position.x = celestial.m00 * f + celestial.m01 * f1 + celestial.m02 * f2 + celestial.m03 * f3;
		position.y = celestial.m10 * f + celestial.m11 * f1 + celestial.m12 * f2 + celestial.m13 * f3;
		position.z = celestial.m20 * f + celestial.m21 * f1 + celestial.m22 * f2 + celestial.m23 * f3;
		position.w = celestial.m30 * f + celestial.m31 * f1 + celestial.m32 * f2 + celestial.m33 * f3;

		return JomlConversions.toJoml(position);
	}

	private static Vector4f getUpPosition() {
		Vector4f upVector = new Vector4f(0.0F, 100.0F, 0.0F, 0.0F);

		// Get the current GBuffer model view matrix, since that is the basis of the celestial model view matrix
		Matrix4f preCelestial = CapturedRenderingState.INSTANCE.getGbufferModelView();

		// Apply the fixed -90.0F degrees rotation to mirror the same transformation in renderSky.
		// But, notably, skip the rotation by the skyAngle.
		preCelestial.rotate(-90.0F, new org.lwjgl.util.vector.Vector3f(0.0F, 1.0F, 0.0F));

		// Use this matrix to transform the vector.
		float f = upVector.x;
		float f1 = upVector.y;
		float f2 = upVector.z;
		float f3 = upVector.w;
		upVector.x = preCelestial.m00 * f + preCelestial.m01 * f1 + preCelestial.m02 * f2 + preCelestial.m03 * f3;
		upVector.y = preCelestial.m10 * f + preCelestial.m11 * f1 + preCelestial.m12 * f2 + preCelestial.m13 * f3;
		upVector.z = preCelestial.m20 * f + preCelestial.m21 * f1 + preCelestial.m22 * f2 + preCelestial.m23 * f3;
		upVector.w = preCelestial.m30 * f + preCelestial.m31 * f1 + preCelestial.m32 * f2 + preCelestial.m33 * f3;

		return JomlConversions.toJoml(upVector);
	}

	public static boolean isDay() {
		// Determine whether it is day or night based on the sky angle.
		//
		// World#isDay appears to do some nontrivial calculations that appear to not entirely work for us here.
		return getSunAngle() <= 0.5;
	}

	private static WorldClient getWorld() {
		return Objects.requireNonNull(Minecraft.getMinecraft().world);
	}

	private static float getSkyAngle() {
		return getWorld().getCelestialAngle(CapturedRenderingState.INSTANCE.getTickDelta());
	}
}
