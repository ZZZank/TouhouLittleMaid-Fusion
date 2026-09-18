package zank.mods.touhou_little_maid_fusion;

import mekanism.common.content.blocktype.Machine;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import zank.mods.touhou_little_maid_fusion.entity.PinSeatEntity;
import zank.mods.touhou_little_maid_fusion.huh.BlockMaidFusionController;
import zank.mods.touhou_little_maid_fusion.huh.TileMaidFusionController;

import java.util.function.Supplier;

import static zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusion.MODID;

/**
 * @author ZZZank
 */
public interface TouhouLittleMaidFusionRegistries {
    BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MODID);
    TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MODID);
    ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MODID);
    EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MODID);
    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    class Block {
        public static final BlockRegistryObject<BlockMaidFusionController, ?> CONTROLLER = BLOCKS
            .register("maid_fusion_controller", BlockMaidFusionController::new);
    }

    class TileEntityType {
        public static final TileEntityTypeRegistryObject<TileMaidFusionController> CONTROLLER = TILE_ENTITY_TYPES
            .mekBuilder(Block.CONTROLLER, TileMaidFusionController::new)
            .build();
    }

    class BlockType {
        public static final Machine<TileMaidFusionController> CONTROLLER = Machine.MachineBuilder
            .createMachine(() -> TileEntityType.CONTROLLER, () -> "todo.lang.key.here")
            .withGui(() -> ContainerType.CONTROLLER)
            .withEnergyConfig(Config.ENERGY_BUFFER_CAPACITY)
            .build();
    }

    class ContainerType {
        public static final ContainerTypeRegistryObject<MekanismTileContainer<TileMaidFusionController>> CONTROLLER = CONTAINER_TYPES
            .register(Block.CONTROLLER, TileMaidFusionController.class);
    }

    class EntityTypes {
        public static final MekanismDeferredHolder<EntityType<?>, EntityType<PinSeatEntity>> HAVE_A_SEAT_PLS = ENTITY_TYPES.registerBuilder(
            "have_a_seat_pls",
            () -> EntityType.Builder.of(PinSeatEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.35f)
                .noSave()
                .noSummon()
        );
    }

    class AttachmentTypes {
        public static final Supplier<AttachmentType<FusionState>> FUSION_STATE = ATTACHMENT_TYPES.register(
            "fusion_state",
            () -> AttachmentType.builder(FusionState::new)
                .serialize(FusionState.CODEC)
                .build()
        );
    }
}
