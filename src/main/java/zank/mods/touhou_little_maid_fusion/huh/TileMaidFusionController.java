package zank.mods.touhou_little_maid_fusion.huh;

import java.util.UUID;
import java.util.function.Predicate;

import mekanism.api.RelativeSide;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableInt;
import net.minecraft.world.entity.Entity;
import mekanism.common.inventory.container.sync.SyncableLong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import net.minecraft.core.BlockPos;
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
public class TileMaidFusionController extends TileEntityGeneratorCopy {

    private BasicInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    @Nullable
    private EntityMaid cachedMaid;

    private int tickCounter = 0;
    private long lastEnergyProduced = 0;

    public TileMaidFusionController(BlockPos pos, BlockState state) {
        super(TouhouLittleMaidFusionRegistries.Blocks.CONTROLLER, pos, state, Config.ENERGY_BUFFER_CAPACITY);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);

        Predicate<@NotNull ItemStack> inputFilter = stack -> stack.is(InitItems.SMART_SLAB_HAS_MAID.get()) || stack.is(InitItems.SMART_SLAB_EMPTY.get());
        inputSlot = builder.addSlot(BasicInventorySlot.at(inputFilter, listener, 146 - 16, 19));
        inputSlot.setSlotOverlay(SlotOverlay.INPUT);

        outputSlot = builder.addSlot(OutputInventorySlot.at(listener, 146 - 16, 51));
        outputSlot.setSlotOverlay(SlotOverlay.OUTPUT);

        return builder.build();
    }

    public BasicInventorySlot getInputSlot() {
        return inputSlot;
    }

    public OutputInventorySlot getOutputSlot() {
        return outputSlot;
    }

    @Nullable
    public EntityMaid getPinnedMaid() {
        if (cachedMaid != null && !cachedMaid.isAlive()) {
            cachedMaid = null;
        }
        return cachedMaid;
    }

    private static final RelativeSide[] SIDES_EXCLUDING_UP = new RelativeSide[]{RelativeSide.BACK, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.TOP, RelativeSide.BOTTOM};

    @NotNull
    @Override
    protected RelativeSide[] getEnergySides() {
        return SIDES_EXCLUDING_UP;
    }

    public long getLastEnergyProduced() {
        return lastEnergyProduced;
    }

    @Override
    public long getProductionRate() {
        return lastEnergyProduced;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);

        // sync maid to client
        container.track(SyncableInt.create(
            () -> this.cachedMaid != null ? this.cachedMaid.getId() : -1,
            (id) -> this.cachedMaid = this.level != null && this.level.getEntity(id) instanceof EntityMaid maid ? maid : null));

        container.track(SyncableLong.create(this::getLastEnergyProduced, value -> lastEnergyProduced = value));
    }

    @Override
    public boolean canFunction() {
        return super.canFunction() && cachedMaid != null;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        tickCounter++;

        var maid = getPinnedMaid();

        if (maid == null) {
            tryRecoverMaid();
        }

        if (maid != null && tickCounter % 20 == 0) {
            validateMaidPresence();
        }

        if (maid == null && inputSlot.getStack().is(InitItems.SMART_SLAB_HAS_MAID) && outputSlot.isEmpty()) {
            tryPinMaid();
        }

        if (maid != null && inputSlot.getStack().is(InitItems.SMART_SLAB_EMPTY) && outputSlot.isEmpty()) {
            tryRecallMaid();
        }

        if (canFunction()) {
            produceEnergy();
        }

        return sendUpdatePacket;
    }

    private void tryRecoverMaid() {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos center = getBlockPos().above(3);
        AABB searchArea = new AABB(center).inflate(2);

        for (EntityMaid maid : serverLevel.getEntitiesOfClass(EntityMaid.class, searchArea, Entity::isAlive)) {
            cachedMaid = maid;
            setChanged();
            break;
        }
    }

    private void tryPinMaid() {
        CustomData maidInfo = inputSlot.getStack().get(InitDataComponent.MAID_INFO);
        if (maidInfo == null || !maidInfo.contains("Owner")) {
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

        outputSlot.insertItem(InitItems.SMART_SLAB_EMPTY.toStack(), Action.EXECUTE, AutomationType.INTERNAL);

        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);

        cachedMaid = maid;
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

        maid.discard();
        cachedMaid = null;
        setChanged();
        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 1.0F, 1.5F);
    }

    private void validateMaidPresence() {
        BlockPos center = getBlockPos().above(3);
        AABB checkArea = new AABB(center).inflate(2);

        EntityMaid maid = getPinnedMaid();
        if (maid == null || !checkArea.intersects(maid.getBoundingBox())) {
            cachedMaid = null;
            setChanged();
        }
    }

    private void produceEnergy() {
        EntityMaid maid = getPinnedMaid();
        if (maid == null) {
            lastEnergyProduced = 0;
            return;
        }

        int fusionState = FusionState.get(maid);
        if (!FusionState.inFusion(fusionState)) {
            lastEnergyProduced = 0;
            return;
        }

        double baseMultiplier = Config.ENERGY_PRODUCTION_MULTIPLIER.getAsDouble();
        double favorabilityMultiplier = Config.FAVORABILITY_ENERGY_MULTIPLIER.getAsDouble();
        double randomPerturbation = Config.RANDOM_PERTURBATION_MULTIPLIER.getAsDouble();

        int favorability = maid.getFavorability();
        double favorabilityFactor = 1.0 + (favorability / 384.0) * favorabilityMultiplier;

        UUID maidUUID = maid.getUUID();
        double uuidPerturbation = 1.0 + (Math.sin(maidUUID.getMostSignificantBits()) * 0.5 + 0.5) * randomPerturbation;

        long totalEnergy = (long) (baseMultiplier * favorabilityFactor * uuidPerturbation);

        if (totalEnergy > 0) {
            getEnergyContainer().insert(totalEnergy, Action.EXECUTE, AutomationType.INTERNAL);
            setActive(true);
            lastEnergyProduced = totalEnergy;
            setChanged();
        } else {
            setActive(false);
            lastEnergyProduced = 0;
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
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }
}
