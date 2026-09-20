package zank.mods.touhou_little_maid_fusion;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.common.registries.MekanismDamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import zank.mods.touhou_little_maid_fusion.util.FusionState;

@Mod(TouhouLittleMaidFusion.MODID)
@EventBusSubscriber
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onMaidHurt(MaidAttackEvent event) {
        if (MekanismDamageTypes.LASER.is(event.getSource())) {
            EntityMaid maid = event.getMaid();

            // Cancel the damage
            event.setCanceled(true);

            int fusionTicks = 20;
            // Enter fusion state
            FusionState.set(maid, fusionTicks);

            var fireResistance = maid.getEffect(MobEffects.FIRE_RESISTANCE);
            if (fireResistance == null || fireResistance.endsWithin(fusionTicks)) {
                maid.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, fusionTicks * 2));
            }
        }
    }

    @SubscribeEvent
    static void onMaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) {
            return;
        }

        int fusionState = FusionState.get(maid);
        if (!FusionState.inFusion(fusionState)) {
            return;
        }

        // Tick fusion state countdown
        FusionState.set(maid, fusionState - 1);
    }
}
