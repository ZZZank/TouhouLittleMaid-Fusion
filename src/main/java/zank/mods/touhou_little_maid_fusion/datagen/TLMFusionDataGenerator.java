package zank.mods.touhou_little_maid_fusion.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusion;

/**
 * @author ZZZank
 */
@EventBusSubscriber(modid = TouhouLittleMaidFusion.MODID)
public class TLMFusionDataGenerator {

    private TLMFusionDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server side data generators
        gen.addProvider(event.includeServer(), new LootTableProvider(
            output,
            java.util.Collections.emptySet(),
            List.of(new LootTableProvider.SubProviderEntry(TLMFusionLootTableGen::new, LootContextParamSets.BLOCK)),
            lookupProvider
        ));
        gen.addProvider(event.includeServer(), new TLMFusionRecipeGen(output, lookupProvider));
    }
}
