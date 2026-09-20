package zank.mods.touhou_little_maid_fusion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusionRegistries;

import java.util.Set;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class TLMFusionLootTableGen extends BlockLootSubProvider {

    protected TLMFusionLootTableGen(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(TouhouLittleMaidFusionRegistries.Blocks.CONTROLLER.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return TouhouLittleMaidFusionRegistries.Blocks.BLOCKS
            .getPrimaryEntries()
            .stream()
            .map((Function<DeferredHolder<Block,? extends Block>, Block>) DeferredHolder::value)
            ::iterator;
    }
}
