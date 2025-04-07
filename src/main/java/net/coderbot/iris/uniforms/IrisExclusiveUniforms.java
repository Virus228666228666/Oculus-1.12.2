package net.coderbot.iris.uniforms;

import net.coderbot.iris.JomlConversions;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.coderbot.iris.vendored.joml.Math;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;

import java.util.Objects;

public class IrisExclusiveUniforms {
	public static void addIrisExclusiveUniforms(UniformHolder uniforms) {
		WorldInfoUniforms.addWorldInfoUniforms(uniforms);

		uniforms.uniform1i(UniformUpdateFrequency.PER_TICK, "currentColorSpace", () -> IrisVideoSettings.colorSpace.ordinal());

		//All Iris-exclusive uniforms (uniforms which do not exist in either OptiFine or ShadersMod) should be registered here.
		uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "thunderStrength", IrisExclusiveUniforms::getThunderStrength);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHealth", IrisExclusiveUniforms::getCurrentHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHealth", IrisExclusiveUniforms::getMaxHealth);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerHunger", IrisExclusiveUniforms::getCurrentHunger);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerHunger", () -> 20);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "currentPlayerAir", IrisExclusiveUniforms::getCurrentAir);
		uniforms.uniform1f(UniformUpdateFrequency.PER_TICK, "maxPlayerAir", IrisExclusiveUniforms::getMaxAir);
		uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "firstPersonCamera", IrisExclusiveUniforms::isFirstPersonCamera);
		uniforms.uniform1b(UniformUpdateFrequency.PER_TICK, "isSpectator", IrisExclusiveUniforms::isSpectator);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "eyePosition", IrisExclusiveUniforms::getEyePosition);
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "relativeEyePosition", () -> CameraUniforms.getUnshiftedCameraPosition().sub(getEyePosition()));
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "playerLookVector", () -> JomlConversions.fromVec3(Minecraft.getMinecraft().getRenderViewEntity().getLookVec()));
		uniforms.uniform3d(UniformUpdateFrequency.PER_FRAME, "playerBodyVector", () -> JomlConversions.fromVec3(Minecraft.getMinecraft().getRenderViewEntity().getForward()));
		Vector4f zero = new Vector4f(0, 0, 0, 0);
		uniforms.uniform4f(UniformUpdateFrequency.PER_TICK, "lightningBoltPosition", () -> {
			if (Minecraft.getMinecraft().world != null) {
				return Minecraft.getMinecraft().world.loadedEntityList.stream().filter(bolt -> bolt instanceof EntityLightningBolt).findAny().map(bolt -> {
					Vector3d unshiftedCameraPosition = CameraUniforms.getUnshiftedCameraPosition();
					Vec3d vec3 = bolt.getPositionVector();
					return new Vector4f((float) (vec3.x - unshiftedCameraPosition.x), (float) (vec3.y - unshiftedCameraPosition.y), (float) (vec3.z - unshiftedCameraPosition.z), 1);
				}).orElse(zero);
			} else {
				return zero;
			}
		});
	}

	private static float getThunderStrength() {
		// Note: Ensure this is in the range of 0 to 1 - some custom servers send out of range values.
		return Math.clamp(0.0F, 1.0F,
			Minecraft.getMinecraft().world.getThunderStrength(CapturedRenderingState.INSTANCE.getTickDelta()));
	}

	private static float getCurrentHealth() {
		if (Minecraft.getMinecraft().player == null || !Minecraft.getMinecraft().playerController.getCurrentGameType().isSurvivalOrAdventure()) {
			return -1;
		}

		return Minecraft.getMinecraft().player.getHealth() / Minecraft.getMinecraft().player.getMaxHealth();
	}

	private static float getCurrentHunger() {
		if (Minecraft.getMinecraft().player == null || !Minecraft.getMinecraft().playerController.getCurrentGameType().isSurvivalOrAdventure()) {
			return -1;
		}

		return Minecraft.getMinecraft().player.getFoodStats().getFoodLevel() / 20f;
	}

	private static float getCurrentAir() {
		if (Minecraft.getMinecraft().player == null || !Minecraft.getMinecraft().playerController.getCurrentGameType().isSurvivalOrAdventure()) {
			return -1;
		}

		return (float) Minecraft.getMinecraft().player.getAir() / 300F/*(float) Minecraft.getMinecraft().player.getMaxAirSupply()*/;
	}

	private static float getMaxAir() {
		if (Minecraft.getMinecraft().player == null || !Minecraft.getMinecraft().playerController.getCurrentGameType().isSurvivalOrAdventure()) {
			return -1;
		}

		return 300F/*Minecraft.getMinecraft().player.getMaxAirSupply()*/;
	}

	private static float getMaxHealth() {
		if (Minecraft.getMinecraft().player == null || !Minecraft.getMinecraft().playerController.getCurrentGameType().isSurvivalOrAdventure()) {
			return -1;
		}

		return Minecraft.getMinecraft().player.getMaxHealth();
	}

	private static boolean isFirstPersonCamera() {
		return Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;
	}

	private static boolean isSpectator() {
		return Minecraft.getMinecraft().playerController.getCurrentGameType() == GameType.SPECTATOR;
	}

	private static Vector3d getEyePosition() {
		Objects.requireNonNull(Minecraft.getMinecraft().getRenderViewEntity());
		Vec3d pos = Minecraft.getMinecraft().getRenderViewEntity().getPositionEyes(CapturedRenderingState.INSTANCE.getTickDelta());
		return new Vector3d(pos.x, pos.y, pos.z);
	}

	public static class WorldInfoUniforms {
		public static void addWorldInfoUniforms(UniformHolder uniforms) {
			WorldClient level = Minecraft.getMinecraft().world;
			// TODO: Use level.dimensionType() coordinates for 1.18!
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "bedrockLevel", () -> 0);
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "cloudHeight", () -> {
				if (level != null) {
					return level.provider.getCloudHeight();
				} else {
					return 192.0;
				}
			});
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "heightLimit", () -> {
				if (level != null) {
					return level.getHeight();
				} else {
					return 256;
				}
			});
			uniforms.uniform1i(UniformUpdateFrequency.PER_FRAME, "logicalHeightLimit", () -> {
				if (level != null) {
					// TODO Correct?
					return level.getActualHeight();
				} else {
					return 256;
				}
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasCeiling", () -> {
				if (level != null) {
					// TODO Not sure, was taken from Angelica, but it seems like we have no other choice
					return level.provider.isNether();
				} else {
					return false;
				}
			});
			uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME, "hasSkylight", () -> {
				if (level != null) {
					return level.provider.hasSkyLight();
				} else {
					return true;
				}
			});
			uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "ambientLight", () -> {
				if (level != null) {
					// TODO Not sure, was taken from Angelica
					return level.provider.getLightBrightnessTable()[0];
				} else {
					return 0f;
				}
			});

		}
	}
}
