package net.coderbot.iris.uniforms;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(FrameUpdateNotifier notifier, DynamicUniformHolder uniforms, IdMap idMap, boolean isOldHandLight) {
		HeldItemSupplier mainHandSupplier = new HeldItemSupplier(EnumHand.MAIN_HAND, idMap.getItemIdMap(), isOldHandLight);
		HeldItemSupplier offHandSupplier = new HeldItemSupplier(EnumHand.OFF_HAND, idMap.getItemIdMap(), false);
		notifier.addListener(mainHandSupplier::update);
		notifier.addListener(offHandSupplier::update);

		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId", mainHandSupplier::getIntID)
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2", offHandSupplier::getIntID)
			.uniform1i(PER_FRAME, "heldBlockLightValue", mainHandSupplier::getLightValue)
			.uniform1i(PER_FRAME, "heldBlockLightValue2", offHandSupplier::getLightValue);
		// TODO: Figure out API.
			//.uniformVanilla3f(PER_FRAME, "heldBlockLightColor", mainHandSupplier::getLightColor)
			//.uniformVanilla3f(PER_FRAME, "heldBlockLightColor2", offHandSupplier::getLightColor);

		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity,
				CapturedRenderingState.INSTANCE.getEntityIdNotifier());

		uniforms.uniform1i("blockEntityId", CapturedRenderingState.INSTANCE::getCurrentRenderedBlockEntity,
				CapturedRenderingState.INSTANCE.getBlockEntityIdNotifier());
	}

	/**
	 * Provides the currently held item, and it's light value, in the given hand as a uniform. Uses the item.properties ID map to map the item
	 * to an integer, and the old hand light value to map offhand to main hand.
	 */
	private static class HeldItemSupplier {
		private final EnumHand hand;
		private final Object2IntFunction<NamespacedId> itemIdMap;
		private final boolean applyOldHandLight;
		private int intID;
		private int lightValue;
		private Vector3f lightColor;

		HeldItemSupplier(EnumHand hand, Object2IntFunction<NamespacedId> itemIdMap, boolean shouldApplyOldHandLight) {
			this.hand = hand;
			this.itemIdMap = itemIdMap;
			this.applyOldHandLight = shouldApplyOldHandLight && hand == EnumHand.MAIN_HAND;
		}

		private void invalidate() {
			intID = -1;
			lightValue = 0;
			lightColor = IrisItemLightProvider.DEFAULT_LIGHT_COLOR;
		}

		public void update() {
			EntityPlayerSP player = Minecraft.getMinecraft().player;

			if (player == null) {
				// Not valid when the player doesn't exist
				invalidate();
				return;
			}

			ItemStack heldStack = player.getHeldItem(hand);

			if (heldStack.isEmpty()) {
				invalidate();
				return;
			}

			Item heldItem = heldStack.getItem();

			if (heldItem == Items.AIR) {
				invalidate();
				return;
			}

			// TODO, maybe it will be better to rewrite on Integer ID's system like it was done in Optifine on 1.12?
			ResourceLocation heldItemId = ForgeRegistries.ITEMS.getKey(heldItem);
			intID = itemIdMap.getInt(new NamespacedId(heldItemId.getNamespace(), heldItemId.getPath()));

			IrisItemLightProvider lightProvider = (IrisItemLightProvider) heldItem;
			lightValue = lightProvider.getLightEmission(Minecraft.getMinecraft().player, heldStack);

			if (applyOldHandLight) {
				lightProvider = applyOldHandLighting(player, lightProvider);
			}

			lightColor = lightProvider.getLightColor(Minecraft.getMinecraft().player, heldStack);
		}

		private IrisItemLightProvider applyOldHandLighting(@Nonnull EntityPlayerSP player, IrisItemLightProvider existing) {
			ItemStack offHandStack = player.getHeldItem(EnumHand.OFF_HAND);

			if (offHandStack.isEmpty()) {
				return existing;
			}

			Item offHandItem = offHandStack.getItem();

			if (offHandItem == Items.AIR) {
				return existing;
			}

			IrisItemLightProvider lightProvider = (IrisItemLightProvider) offHandItem;
			int newEmission = lightProvider.getLightEmission(Minecraft.getMinecraft().player, offHandStack);

			if (lightValue < newEmission) {
				lightValue = newEmission;
				return lightProvider;
			}

			return existing;
		}

		public int getIntID() {
			return intID;
		}

		public int getLightValue() {
			return lightValue;
		}

		public Vector3f getLightColor() {
			return lightColor;
		}
	}
}
