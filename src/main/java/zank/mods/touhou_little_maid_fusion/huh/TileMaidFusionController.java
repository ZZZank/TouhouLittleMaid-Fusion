package zank.mods.touhou_little_maid_fusion.huh;

import java.util.UUID;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import zank.mods.touhou_little_maid_fusion.Config;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusionRegistries;
import zank.mods.touhou_little_maid_fusion.entity.PinSeatEntity;
import zank.mods.touhou_little_maid_fusion.util.FusionState;

/**
 * @author ZZZank
 */
public class TileMaidFusionController extends TileEntityMekanism {

    private MachineEnergyContainer<TileMaidFusionController> energyContainer;
    private BasicInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    @Nullable
    private UUID pinnedMaidUUID;
    @Nullable
    private EntityMaid cachedMaid;
    @Nullable
    private PinSeatEntity pinSeatEntity;
    private boolean running = false;
    private int tickCounter = 0;
    private long lastEnergyProduced = 0;

    public TileMaidFusionController(BlockPos pos, BlockState state) {
        super(TouhouLittleMaidFusionRegistries.Blocks.CONTROLLER, pos, state);
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
        inputSlot = builder.addSlot(BasicInventorySlot.at(
            stack -> stack.is(InitItems.SMART_SLAB_HAS_MAID.get()) || stack.is(InitItems.SMART_SLAB_EMPTY.get()),
            listener,
            146 - 16,
            19
        ));
        outputSlot = builder.addSlot(OutputInventorySlot.at(listener, 146 - 16, 51));
        inputSlot.setSlotOverlay(SlotOverlay.INPUT);
        outputSlot.setSlotOverlay(SlotOverlay.OUTPUT);
        return builder.build();
    }

    public MachineEnergyContainer<TileMaidFusionController> getEnergyContainer() {
        return energyContainer;
    }

    public BasicInventorySlot getInputSlot() {
        return inputSlot;
    }

    public OutputInventorySlot getOutputSlot() {
        return outputSlot;
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

    @Nullable
    public PinSeatEntity getPinSeatEntity() {
        return pinSeatEntity;
    }

    public boolean isRunning() {
        return running;
    }

    public long getLastEnergyProduced() {
        return lastEnergyProduced;
    }

    @Nullable
    public EntityMaid getCachedMaid() {
        return cachedMaid;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::isRunning, value -> running = value));
        container.track(SyncableLong.create(this::getLastEnergyProduced, value -> lastEnergyProduced = value));
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        tickCounter++;

        if (tickCounter % 20 == 0) {
            validateMaidPresence();
        }

        if (pinnedMaidUUID == null && inputSlot.getStack().is(InitItems.SMART_SLAB_HAS_MAID) && outputSlot.isEmpty()) {
            tryPinMaid();
        }

        if (pinnedMaidUUID != null && inputSlot.getStack().is(InitItems.SMART_SLAB_EMPTY) && outputSlot.isEmpty()) {
            tryRecallMaid();
        }

        if (running && pinnedMaidUUID != null) {
            produceEnergy();
        }

        pushEnergy();
        return sendUpdatePacket;
    }

    private void tryPinMaid() {
        CustomData maidInfo = inputSlot.getStack().get(InitDataComponent.MAID_INFO);
        if (maidInfo == null || !maidInfo.contains("Owner")) {
            // @see com.github.tartaricacid.touhoulittlemaid.item.AbstractStoreMaidItem#spawnFromStore(...)
            return;
        }

        inputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);

        BlockPos spawnPos = getBlockPos().above(3);
        EntityMaid maid = new EntityMaid(level);
        maid.load(maidInfo.copyTag());
        maid.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

        level.addFreshEntity(maid);

        PinSeatEntity seat = new PinSeatEntity(TouhouLittleMaidFusionRegistries.EntityTypes.HAVE_A_SEAT_PLS.get(), level);
        seat.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(seat);
        maid.startRiding(seat);
        pinSeatEntity = seat;

        outputSlot.insertItem(InitItems.SMART_SLAB_EMPTY.toStack(), Action.EXECUTE, AutomationType.INTERNAL);

        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);

        pinnedMaidUUID = maid.getUUID();
        running = true;
        setChanged();
    }

    private void tryRecallMaid() {
        EntityMaid maid = getPinnedMaid();
        if (maid == null) {
            return;
        }

        inputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);

        ItemStack newSoulCard = new ItemStack(InitItems.SMART_SLAB_HAS_MAID.get());
        CompoundTag entityData = new CompoundTag();
        maid.saveWithoutId(entityData);
        newSoulCard.set(InitDataComponent.MAID_INFO, CustomData.of(entityData));

        outputSlot.insertItem(newSoulCard, Action.EXECUTE, AutomationType.INTERNAL);

        // 销毁PinSeatEntity（会自动让女仆下骑乘）
        if (pinSeatEntity != null) {
            pinSeatEntity.discard();
            pinSeatEntity = null;
        }

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
        boolean maidValid = maid != null && checkArea.intersects(maid.getBoundingBox());
        boolean seatValid = pinSeatEntity != null && pinSeatEntity.isAlive();

        if (!maidValid || !seatValid) {
            running = false;
            pinnedMaidUUID = null;
            cachedMaid = null;
            if (pinSeatEntity != null) {
                pinSeatEntity.discard();
                pinSeatEntity = null;
            }
            setChanged();
        }
    }

    private void produceEnergy() {
        EntityMaid maid = getPinnedMaid();
        if (maid == null) {
            running = false;
            return;
        }

        int fusionState = FusionState.get(maid);
        if (!FusionState.inFusion(fusionState)) {
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
            lastEnergyProduced = totalEnergy;
            setChanged();
        } else {
            lastEnergyProduced = 0;
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
        if (!inputSlot.isEmpty()) {
            Containers.dropItemStack(
                level,
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 0.5,
                getBlockPos().getZ() + 0.5,
                inputSlot.getStack()
            );
        }
        if (!outputSlot.isEmpty()) {
            Containers.dropItemStack(
                level,
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + 0.5,
                getBlockPos().getZ() + 0.5,
                outputSlot.getStack()
            );
        }
        // 销毁PinSeatEntity
        if (pinSeatEntity != null) {
            pinSeatEntity.discard();
            pinSeatEntity = null;
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