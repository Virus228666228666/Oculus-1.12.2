package net.coderbot.iris.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderGlobal.class)
public interface RenderGlobalAccessor {
	@Accessor("renderManager")
	RenderManager getRenderManager();

	@Invoker("renderBlockLayer")
	void invokeRenderBlockLayer(BlockRenderLayer blockLayerIn);

	@Invoker("setupTerrain")
//	void invokeSetupRender(EntityRenderer camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);
	void invokeSetupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator);
//
//	@Invoker("renderEntity")
//	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta);

	@Accessor("world")
	WorldClient getWorld();

//	@Accessor("generateClouds")
//	boolean shouldRegenerateClouds();
//
//	@Accessor("generateClouds")
//	void setShouldRegenerateClouds(boolean shouldRegenerate);
}
