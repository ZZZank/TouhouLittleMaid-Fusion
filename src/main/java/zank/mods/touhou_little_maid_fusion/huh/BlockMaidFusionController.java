package zank.mods.touhou_little_maid_fusion.huh;

import mekanism.common.block.prefab.BlockTile;
import mekanism.generators.common.content.blocktype.Generator;
import net.minecraft.world.level.material.MapColor;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusionRegistries;

/**
 * @author ZZZank
 */
public class BlockMaidFusionController extends BlockTile<TileMaidFusionController, Generator<TileMaidFusionController>> {
    public BlockMaidFusionController() {
        super(TouhouLittleMaidFusionRegistries.BlockTypes.CONTROLLER, p -> p.mapColor(MapColor.COLOR_ORANGE));
    }
}
