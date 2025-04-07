package net.coderbot.iris.pipeline;

import net.minecraft.util.BlockRenderLayer;

public enum WorldRenderingPhase {
	NONE,
	SKY,
	SUNSET,
	CUSTOM_SKY, // Unused, just here to match OptiFine ordinals
	SUN,
	MOON,
	STARS,
	VOID,
	TERRAIN_SOLID,
	TERRAIN_CUTOUT_MIPPED,
	TERRAIN_CUTOUT,
	ENTITIES,
	BLOCK_ENTITIES,
	DESTROY,
	OUTLINE,
	DEBUG,
	HAND_SOLID,
	TERRAIN_TRANSLUCENT,
	PARTICLES,
	CLOUDS,
	RAIN_SNOW,
	WORLD_BORDER,
	HAND_TRANSLUCENT;

	public static WorldRenderingPhase fromTerrainRenderType(BlockRenderLayer renderType) {
		if (renderType == BlockRenderLayer.SOLID) {
			return WorldRenderingPhase.TERRAIN_SOLID;
		} else if (renderType == BlockRenderLayer.CUTOUT) {
			return WorldRenderingPhase.TERRAIN_CUTOUT;
		} else if (renderType == BlockRenderLayer.CUTOUT_MIPPED) {
			return WorldRenderingPhase.TERRAIN_CUTOUT_MIPPED;
		} else if (renderType == BlockRenderLayer.TRANSLUCENT) {
			return WorldRenderingPhase.TERRAIN_TRANSLUCENT;
		} else {
			throw new IllegalStateException("Illegal render type!");
		}
	}
}
