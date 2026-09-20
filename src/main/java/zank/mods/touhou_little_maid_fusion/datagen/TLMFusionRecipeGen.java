package zank.mods.touhou_little_maid_fusion.datagen;

import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import java.util.concurrent.CompletableFuture;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusion;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusionRegistries;

/**
 * @author ZZZank
 */
public class TLMFusionRecipeGen extends RecipeProvider {
    public TLMFusionRecipeGen(
        PackOutput output,
        CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TouhouLittleMaidFusionRegistries.Blocks.CONTROLLER.asItem())
            .pattern("ASA")
            .pattern("HDH")
            .pattern("APA")
            .define('D', MekanismBlocks.DIMENSIONAL_STABILIZER.asItem())
            .define('S', GeneratorsBlocks.FUSION_REACTOR_PORT.asItem())
            .define('A', MekanismItems.ATOMIC_ALLOY)
            .define('P', MekanismItems.PLUTONIUM_PELLET)
            .define('H', GeneratorsBlocks.FUSION_REACTOR_FRAME.asItem())
            .unlockedBy(getHasName(InitItems.SMART_SLAB_HAS_MAID.get()), has(InitItems.SMART_SLAB_HAS_MAID.get()))
            .save(recipeOutput, TouhouLittleMaidFusion.rl("maid_fusion_controller"));
    }
}
