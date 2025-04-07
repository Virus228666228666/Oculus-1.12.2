package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.materialmap.BlockEntry;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockMaterialMapping {
	public static Object2IntMap<IBlockState> createBlockStateIdMap(Int2ObjectMap<List<BlockEntry>> blockPropertiesMap) {
		Object2IntMap<IBlockState> blockStateIds = new Object2IntOpenHashMap<>();

		blockPropertiesMap.forEach((intId, entries) -> {
			for (BlockEntry entry : entries) {
				addBlockStates(entry, blockStateIds, intId);
			}
		});

		return blockStateIds;
	}

	public static Map<IRegistryDelegate<Block>, BlockRenderLayer> createBlockTypeMap(Map<NamespacedId, BlockRenderType> blockPropertiesMap) {
		Map<IRegistryDelegate<Block>, BlockRenderLayer> blockTypeIds = new Object2ObjectOpenHashMap<>();

		blockPropertiesMap.forEach((id, blockType) -> {
			ResourceLocation resourceLocation = new ResourceLocation(id.getNamespace(), id.getName());

			Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);

			if (block == null || block == Blocks.AIR) {
				return;
			}

			blockTypeIds.put(block.delegate, convertBlockToRenderType(blockType));
		});

		return blockTypeIds;
	}

	private static BlockRenderLayer convertBlockToRenderType(BlockRenderType type) {
		if (type == null) {
			return null;
		}

		switch (type) {
			case SOLID: return BlockRenderLayer.SOLID;
			case CUTOUT: return BlockRenderLayer.CUTOUT;
			case CUTOUT_MIPPED: return BlockRenderLayer.CUTOUT_MIPPED;
			case TRANSLUCENT: return BlockRenderLayer.TRANSLUCENT;
			default: return null;
		}
	}

	private static void addBlockStates(BlockEntry entry, Object2IntMap<IBlockState> idMap, int intId) {
		NamespacedId id = entry.getId();
		ResourceLocation resourceLocation = new ResourceLocation(id.getNamespace(), id.getName());

		Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);

		// If the block doesn't exist, by default the registry will return AIR. That probably isn't what we want.
		// TODO: Assuming that Registry.BLOCK.getDefaultId() == "minecraft:air" here
		if (block == null || block == Blocks.AIR) {
			return;
		}

		Map<String, String> propertyPredicates = entry.getPropertyPredicates();

		if (propertyPredicates.isEmpty()) {
			// Just add all the states if there aren't any predicates
			for (IBlockState state : block.getBlockState().getValidStates()) {
				// NB: Using putIfAbsent means that the first successful mapping takes precedence
				//     Needed for OptiFine parity:
				//     https://github.com/IrisShaders/Iris/issues/1327
				idMap.putIfAbsent(state, intId);
			}

			return;
		}

		// As a result, we first collect each key=value pair in order to determine what properties we need to filter on.
		// We already get this from BlockEntry, but we convert the keys to `Property`s to ensure they exist and to avoid
		// string comparisons later.
		Map<IProperty<?>, String> properties = new HashMap<>();
		BlockStateContainer stateManager = block.getBlockState();

		propertyPredicates.forEach((key, value) -> {
			IProperty<?> property = stateManager.getProperty(key);

			if (property == null) {
				Iris.logger.warn("Error while parsing the block ID map entry for \"" + "block." + intId + "\":");
				Iris.logger.warn("- The block " + resourceLocation + " has no property with the name " + key + ", ignoring!");

				return;
			}

			properties.put(property, value);
		});

		// Once we have a list of properties and their expected values, we iterate over every possible state of this
		// block and check for ones that match the filters. This isn't particularly efficient, but it works!
		for (IBlockState state : stateManager.getValidStates()) {
			if (checkState(state, properties)) {
				// NB: Using putIfAbsent means that the first successful mapping takes precedence
				//     Needed for OptiFine parity:
				//     https://github.com/IrisShaders/Iris/issues/1327
				idMap.putIfAbsent(state, intId);
			}
		}
	}

	// We ignore generics here, the actual types don't matter because we just convert
	// them to strings anyways, and the compiler checks just get in the way.
	//
	// If you're able to rewrite this function without SuppressWarnings, feel free.
	// But otherwise it works fine.
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean checkState(IBlockState state, Map<IProperty<?>, String> expectedValues) {
		for (Map.Entry<IProperty<?>, String> condition : expectedValues.entrySet()) {
			IProperty property = condition.getKey();
			String expectedValue = condition.getValue();

			String actualValue = property.getName(state.getValue(property));

			if (!expectedValue.equals(actualValue)) {
				return false;
			}
		}

		return true;
	}
}
