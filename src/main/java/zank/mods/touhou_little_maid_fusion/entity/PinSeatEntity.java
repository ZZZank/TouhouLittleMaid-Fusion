package zank.mods.touhou_little_maid_fusion.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/// For "pinning" the maid
public class PinSeatEntity extends Entity {

    public PinSeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        var bb = getBoundingBox();
        Vec3 diff = new Vec3(x, y, z).subtract(bb.getCenter());
        setBoundingBox(bb.move(diff));
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, Entity.@NotNull MoveFunction callback) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        callback.accept(passenger, this.getX(), this.getY(), this.getZ());
    }

    @Override
    public void onPassengerTurned(Entity entity) {
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 vec) {
        // 阻止移动
    }

    @Override
    public void tick() {
        if (this.getFirstPassenger() == null) {
            this.discard();
        }
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return true;
    }

    @Override
    protected void removePassenger(@NotNull Entity entity) {
        super.removePassenger(entity);
        if (!level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }
}
