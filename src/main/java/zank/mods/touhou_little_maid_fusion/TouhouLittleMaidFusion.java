package zank.mods.touhou_little_maid_fusion;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(TouhouLittleMaidFusion.MODID)
public class TouhouLittleMaidFusion {
    public static final String MODID = "touhou_little_maid_fusion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public TouhouLittleMaidFusion(IEventBus modEventBus, ModContainer modContainer) {
        // Register Mekanism-style registries
        TouhouLittleMaidFusionRegistries.Blocks.BLOCKS.register(modEventBus);
        TouhouLittleMaidFusionRegistries.TileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        TouhouLittleMaidFusionRegistries.ContainerTypes.CONTAINER_TYPES.register(modEventBus);
        TouhouLittleMaidFusionRegistries.EntityTypes.ENTITY_TYPES.register(modEventBus);

        // Register NeoForge attachment types
        TouhouLittleMaidFusionRegistries.AttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
