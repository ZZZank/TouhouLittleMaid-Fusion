package zank.mods.touhou_little_maid_fusion.huh;

import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import zank.mods.touhou_little_maid_fusion.Config;
import zank.mods.touhou_little_maid_fusion.FusionState;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusionRegistries;

/**
 * @author ZZZank
 */
public class TileMaidFusionController extends TileEntityMekanism {

    private MachineEnergyContainer<TileMaidFusionController> energyContainer;
    private BasicInventorySlot maidInputSlot;
    private BasicInventorySlot maidOutputSlot;

    @Nullable
    private UUID pinnedMaidUUID;
    @Nullable
    private EntityMaid cachedMaid;
    private boolean running = false;
    private int tickCounter = 0;

    public TileMaidFusionController(BlockPos pos, BlockState state) {
        super(TouhouLittleMaidFusionRegistries.Block.CONTROLLER, pos, state);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        // "-" slot: soul card with maid
        Predicate<ItemStack> maidCardValidator = stack -> stack.is(InitItems.SMART_SLAB_HAS_MAID.get());
        builder.addSlot(maidInputSlot = BasicInventorySlot.at(maidCardValidator, listener, 56, 35));
        // "+" slot: empty soul card
        Predicate<ItemStack> emptyCardValidator = stack -> stack.is(InitItems.SMART_SLAB_EMPTY.get());
        builder.addSlot(maidOutputSlot = BasicInventorySlot.at(emptyCardValidator, listener, 100, 35));
        return builder.build();
    }

    public MachineEnergyContainer<TileMaidFusionController> getEnergyContainer() {
        return energyContainer;
    }

    public BasicInventorySlot getMaidInputSlot() {
        return maidInputSlot;
    }

    public BasicInventorySlot getMaidOutputSlot() {
        return maidOutputSlot;
    }

    @Nullable
    public EntityMaid getPinnedMaid() {
        if (pinnedMaidUUID == null || level == null) {
            return null;
        }
        if (cachedMaid != null && cachedMaid.isAlive() && cachedMaid.getUUID().equals(pinnedMaidUUID)) {
            return cachedMaid;
        }
        if (level instanceof ServerLevel serverLevel) {
            for (EntityMaid maid : serverLevel.getEntitiesOfClass(
                EntityMaid.class,
                new AABB(getBlockPos()).inflate(16),
                m -> m.getUUID().equals(pinnedMaidUUID)
            )) {
                cachedMaid = maid;
                return maid;
            }
        }
        cachedMaid = null;
        return null;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        tickCounter++;

        if (tickCounter % 20 == 0) {
            validateMaidPresence();
        }

        if (pinnedMaidUUID == null) {
            tryPinMaid();
        }

        if (pinnedMaidUUID != null) {
            tryRecallMaid();
        }

        if (running && pinnedMaidUUID != null) {
            produceEnergy();
        }

        pushEnergy();
        return sendUpdatePacket;
    }

    private void tryPinMaid() {
        ItemStack minusSlot = maidInputSlot.getStack();
        if (minusSlot.isEmpty() || !minusSlot.is(InitItems.SMART_SLAB_HAS_MAID.get())) {
            return;
        }

        CustomData maidInfo = minusSlot.get(InitDataComponent.MAID_INFO);
        if (maidInfo == null) {
            return;
        }

        ItemStack soulCard = maidInputSlot.extractItem(1, Action.SIMULATE, AutomationType.INTERNAL);
        if (soulCard.isEmpty()) {
            return;
        }
        maidInputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);

        BlockPos spawnPos = getBlockPos().above(3);
        EntityMaid maid = new EntityMaid(level);
        maid.load(maidInfo.copyTag());
        maid.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

        for (Player player : level.players()) {
            if (player.distanceToSqr(
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 0.5,
                getBlockPos().getZ() + 0.5
            ) < 64) {
                maid.tame(player);
                break;
            }
        }

        level.addFreshEntity(maid);
        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);

        pinnedMaidUUID = maid.getUUID();
        running = true;
        setChanged();
    }

    private void tryRecallMaid() {
        ItemStack plusSlot = maidOutputSlot.getStack();
        if (plusSlot.isEmpty() || !plusSlot.is(InitItems.SMART_SLAB_EMPTY.get()) || pinnedMaidUUID == null) {
            return;
        }

        EntityMaid maid = getPinnedMaid();
        if (maid == null) {
            return;
        }

        maidOutputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);

        ItemStack newSoulCard = new ItemStack(InitItems.SMART_SLAB_HAS_MAID.get());
        CompoundTag entityData = new CompoundTag();
        maid.saveWithoutId(entityData);
        newSoulCard.set(InitDataComponent.MAID_INFO, CustomData.of(entityData));

        ItemStack remainder = maidInputSlot.insertItem(newSoulCard, Action.SIMULATE, AutomationType.INTERNAL);
        if (!remainder.isEmpty()) {
            Containers.dropItemStack(
                level,
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 1.5,
                getBlockPos().getZ() + 0.5,
                remainder
            );
        }
        maidInputSlot.insertItem(newSoulCard, Action.EXECUTE, AutomationType.INTERNAL);

        maid.discard();
        pinnedMaidUUID = null;
        cachedMaid = null;
        running = false;
        setChanged();
        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 1.0F, 1.5F);
    }

    private void validateMaidPresence() {
        if (pinnedMaidUUID == null) {
            running = false;
            return;
        }

        BlockPos center = getBlockPos().above(3);
        AABB checkArea = new AABB(center).inflate(1);

        EntityMaid maid = getPinnedMaid();
        if (maid == null || !checkArea.intersects(maid.getBoundingBox())) {
            running = false;
            pinnedMaidUUID = null;
            cachedMaid = null;
            setChanged();
        }
    }

    private void produceEnergy() {
        EntityMaid maid = getPinnedMaid();
        if (maid == null) {
            running = false;
            return;
        }

        FusionState fusionState = maid.getData(FusionState.TYPE.get());
        if (!fusionState.isInFusion()) {
            return;
        }

        int currentHunger = maid.getHunger();
        if (currentHunger <= 0) {
            return;
        }

        double baseMultiplier = Config.ENERGY_PRODUCTION_MULTIPLIER.getAsDouble();
        double favorabilityMultiplier = Config.FAVORABILITY_ENERGY_MULTIPLIER.getAsDouble();
        double hungerMultiplier = Config.HUNGER_ENERGY_MULTIPLIER.getAsDouble();
        double randomPerturbation = Config.RANDOM_PERTURBATION_MULTIPLIER.getAsDouble();

        int favorability = maid.getFavorability();
        double favorabilityFactor = 1.0 + (favorability / 384.0) * favorabilityMultiplier;
        double hungerFactor = 1.0 + (currentHunger / 100.0) * hungerMultiplier;

        UUID maidUUID = maid.getUUID();
        double uuidPerturbation = 1.0 + (Math.sin(maidUUID.getMostSignificantBits()) * 0.5 + 0.5) * randomPerturbation;

        long totalEnergy = (long) (baseMultiplier * favorabilityFactor * hungerFactor * uuidPerturbation);

        if (totalEnergy > 0) {
            energyContainer.insert(totalEnergy, Action.EXECUTE, AutomationType.INTERNAL);
            setChanged();
        }
    }

    private void pushEnergy() {
        if (level == null || energyContainer.isEmpty()) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = getBlockPos().relative(direction);
            var cap = level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                neighborPos,
                direction.getOpposite()
            );
            if (cap != null && cap.canReceive()) {
                long extracted = energyContainer.extract(
                    energyContainer.getEnergy(),
                    Action.SIMULATE,
                    AutomationType.INTERNAL
                );
                if (extracted > 0) {
                    int inserted = cap.receiveEnergy(
                        (int) Math.min(extracted, Integer.MAX_VALUE),
                        false
                    );
                    if (inserted > 0) {
                        energyContainer.extract(inserted, Action.EXECUTE, AutomationType.INTERNAL);
                    }
                }
            }
        }
    }

    public void dropItems() {
        if (level == null) {
            return;
        }
        if (!maidInputSlot.isEmpty()) {
            Containers.dropItemStack(
                level,
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 0.5,
                getBlockPos().getZ() + 0.5,
                maidInputSlot.getStack()
            );
        }
        if (!maidOutputSlot.isEmpty()) {
            Containers.dropItemStack(
                level,
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 0.5,
                getBlockPos().getZ() + 0.5,
                maidOutputSlot.getStack()
            );
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (pinnedMaidUUID != null) {
            tag.putUUID("PinnedMaid", pinnedMaidUUID);
        }
        tag.putBoolean("Running", running);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("PinnedMaid")) {
            pinnedMaidUUID = tag.getUUID("PinnedMaid");
        }
        running = tag.getBoolean("Running");
    }
}
