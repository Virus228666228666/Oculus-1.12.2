package net.coderbot.iris.pipeline;

import lombok.Getter;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.GameType;
import org.lwjgl.opengl.GL11;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	private boolean ACTIVE;
	@Getter
    private boolean renderingSolid;

	public static final float DEPTH = 0.125F;

	private void setupGlState(EntityRenderer renderer, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		float aspectRatio = (float) Minecraft.getMinecraft().displayWidth / (float) Minecraft.getMinecraft().displayHeight;
		GL11.glFrustum(-aspectRatio, aspectRatio, -1.0, 1.0, 0.05, 1000.0);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		if (renderer != null) {
			renderer.renderWorld(partialTicks, 2);
		}
	}

	private boolean canRender(EntityRenderer renderer) {
		Minecraft mc = Minecraft.getMinecraft();
		Entity entity = mc.getRenderViewEntity();
		if (!(entity instanceof EntityPlayerSP)) {
			return false;
		}

		EntityPlayerSP player = (EntityPlayerSP) entity;
		GameSettings settings = mc.gameSettings;
		// settings.thirdPersonView == 0 == is first-person
		return !(settings.thirdPersonView == 0 && player.isPlayerSleeping() || mc.playerController.getCurrentGameType() == GameType.SPECTATOR);
	}

	public boolean isHandTranslucent(EnumHand hand) {
		Item item = Minecraft.getMinecraft().player.getHeldItem(hand).getItem();

		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			return block.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
		}

		return false;
	}

	public boolean isAnyHandTranslucent() {
		return isHandTranslucent(EnumHand.MAIN_HAND) || isHandTranslucent(EnumHand.OFF_HAND);
	}

	public void renderSolid(float partialTicks, EntityRenderer renderer) {
		if (!canRender(renderer) || !IrisApi.getInstance().isShaderPackInUse()) {
			return;
		}

		ACTIVE = true;

		Minecraft mc = Minecraft.getMinecraft();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		EntityPlayerSP player = mc.player;

		GL11.glPushMatrix();
		mc.profiler.startSection("iris_hand");

		setupGlState(renderer, partialTicks);

		renderingSolid = true;

		// todo floats might be wrong, unsure
		itemRenderer.renderItemInFirstPerson(player, partialTicks, 1, EnumHand.MAIN_HAND, partialTicks, player.getHeldItem(EnumHand.MAIN_HAND), 0.0f);
		itemRenderer.renderItemInFirstPerson(player, partialTicks, 1, EnumHand.OFF_HAND, partialTicks, player.getHeldItem(EnumHand.OFF_HAND), 0.0f);

		mc.profiler.endSection();
		GL11.glPopMatrix();

		renderingSolid = false;

		ACTIVE = false;
	}

	public void renderTranslucent(float partialTicks, EntityRenderer renderer) {
		if (!canRender(renderer) || !isAnyHandTranslucent() || !IrisApi.getInstance().isShaderPackInUse()) {
			return;
		}

		ACTIVE = true;

		Minecraft mc = Minecraft.getMinecraft();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		EntityPlayerSP player = mc.player;

		GL11.glPushMatrix();
		mc.profiler.startSection("iris_hand_translucent");

		setupGlState(renderer, partialTicks);

		itemRenderer.renderItemInFirstPerson(player, partialTicks, 1, EnumHand.MAIN_HAND, partialTicks, player.getHeldItem(EnumHand.MAIN_HAND), 0.0f);
		itemRenderer.renderItemInFirstPerson(player, partialTicks, 1, EnumHand.OFF_HAND, partialTicks, player.getHeldItem(EnumHand.OFF_HAND), 0.0f);

		mc.profiler.endSection();
		GL11.glPopMatrix();

		ACTIVE = false;
	}

	public boolean isActive() {
		return ACTIVE;
	}
}
