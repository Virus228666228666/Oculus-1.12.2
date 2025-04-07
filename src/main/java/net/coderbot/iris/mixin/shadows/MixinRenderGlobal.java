package net.coderbot.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coderbot.iris.shadows.CullingDataCache;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal implements CullingDataCache {

	// TODO Let's replace with FastUtils' collection
	@Shadow
	@Mutable
	private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = new ObjectArrayList<>(69696);

	@Unique
	private List<RenderGlobal.ContainerLocalRenderInformation> savedRenderChunks = new ObjectArrayList<>(69696);

	@Shadow
	private boolean displayListEntitiesDirty;

	@Unique
	private boolean savedNeedsTerrainUpdate;

	@Shadow
	private double frustumUpdatePosX;

	@Shadow
	private double frustumUpdatePosY;

	@Shadow
	private double frustumUpdatePosZ;

	@Shadow
	private double lastViewEntityPitch;

	@Shadow
	private double lastViewEntityYaw;

	@Unique
	private double savedLastCameraX;

	@Unique
	private double savedLastCameraY;

	@Unique
	private double savedLastCameraZ;

	@Unique
	private double savedLastCameraPitch;

	@Unique
	private double savedLastCameraYaw;

	@Override
	public void saveState() {
		swap();
	}

	@Override
	public void restoreState() {
		swap();
	}

	@Unique
	private void swap() {
		List<RenderGlobal.ContainerLocalRenderInformation> tmpList = renderInfos;
		renderInfos = savedRenderChunks;
		savedRenderChunks = tmpList;

		// TODO: If the normal chunks need a terrain update, these chunks probably do too...
		// We probably should copy it over
		boolean tmpBool = displayListEntitiesDirty;
		displayListEntitiesDirty = savedNeedsTerrainUpdate;
		savedNeedsTerrainUpdate = tmpBool;

		double tmp;

		tmp = frustumUpdatePosX;
		frustumUpdatePosX = savedLastCameraX;
		savedLastCameraX = tmp;

		tmp = frustumUpdatePosY;
		frustumUpdatePosY = savedLastCameraY;
		savedLastCameraY = tmp;

		tmp = frustumUpdatePosZ;
		frustumUpdatePosZ = savedLastCameraZ;
		savedLastCameraZ = tmp;

		tmp = lastViewEntityPitch;
		lastViewEntityPitch = savedLastCameraPitch;
		savedLastCameraPitch = tmp;

		tmp = lastViewEntityYaw;
		lastViewEntityYaw = savedLastCameraYaw;
		savedLastCameraYaw = tmp;
	}
}
