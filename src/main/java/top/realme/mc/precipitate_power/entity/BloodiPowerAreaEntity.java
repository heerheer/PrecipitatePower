package top.realme.mc.precipitate_power.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import top.realme.mc.precipitate_power.registry.ModEffects;
import top.realme.mc.precipitate_power.registry.ModEntities;

import java.util.UUID;

public class BloodiPowerAreaEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(BloodiPowerAreaEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_TICKS =
            SynchedEntityData.defineId(BloodiPowerAreaEntity.class, EntityDataSerializers.INT);

    private UUID ownerUuid;

    public BloodiPowerAreaEntity(EntityType<? extends BloodiPowerAreaEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        noCulling = true;
    }

    public BloodiPowerAreaEntity(Level level, LivingEntity owner, float radius, int durationTicks) {
        this(ModEntities.BLOODI_POWER_AREA.get(), level);
        ownerUuid = owner.getUUID();
        setRadius(radius);
        setRemainingTicks(durationTicks);
        setPos(owner.getX(), owner.getY() + 0.03D, owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 4.0F);
        builder.define(DATA_REMAINING_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        int remainingTicks = getRemainingTicks() - 1;
        setRemainingTicks(remainingTicks);
        if (remainingTicks <= 0) {
            discard();
            return;
        }

        if ((tickCount - 1) % 10 != 0 || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity ownerEntity = ownerUuid == null ? null : serverLevel.getEntity(ownerUuid);
        if (!(ownerEntity instanceof LivingEntity owner) || !owner.isAlive()) {
            discard();
            return;
        }

        float radius = getRadius();
        AABB area = new AABB(
                getX() - radius, getY() - 1.5D, getZ() - radius,
                getX() + radius, getY() + 3.0D, getZ() + radius);
        double radiusSquared = radius * radius;
        for (LivingEntity candidate : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && !entity.isSpectator())) {
            double dx = candidate.getX() - getX();
            double dz = candidate.getZ() - getZ();
            if (dx * dx + dz * dz <= radiusSquared && isFriendly(owner, candidate)) {
                candidate.addEffect(new MobEffectInstance(
                        ModEffects.BLOODI_POWER, 25, 0, false, false, true));
            }
        }
    }

    private static boolean isFriendly(LivingEntity owner, LivingEntity candidate) {
        if (candidate == owner || owner.isAlliedTo(candidate)) {
            return true;
        }
        if (candidate instanceof OwnableEntity ownable) {
            LivingEntity petOwner = ownable.getOwner();
            return petOwner != null && (petOwner == owner || owner.isAlliedTo(petOwner));
        }
        return false;
    }

    public float getRadius() {
        return entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        entityData.set(DATA_RADIUS, Math.max(0.5F, radius));
    }

    public int getRemainingTicks() {
        return entityData.get(DATA_REMAINING_TICKS);
    }

    public void setRemainingTicks(int remainingTicks) {
        entityData.set(DATA_REMAINING_TICKS, Math.max(0, remainingTicks));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putFloat("Radius", getRadius());
        tag.putInt("RemainingTicks", getRemainingTicks());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        setRadius(tag.getFloat("Radius"));
        setRemainingTicks(tag.getInt("RemainingTicks"));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
