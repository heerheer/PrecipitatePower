package top.realme.mc.precipitate_power.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import top.realme.mc.precipitate_power.registry.ModEntities;

public class HomingSockProjectileEntity extends AbstractSockProjectileEntity {
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(HomingSockProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ARC_ANGLE =
            SynchedEntityData.defineId(HomingSockProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ARC_STRENGTH =
            SynchedEntityData.defineId(HomingSockProjectileEntity.class, EntityDataSerializers.FLOAT);

    private static final double FLIGHT_SPEED = 0.72D;
    private static final double ARC_DURATION_TICKS = 28.0D;

    public HomingSockProjectileEntity(EntityType<? extends HomingSockProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public HomingSockProjectileEntity(Level level, LivingEntity owner, Entity target,
                                      float damage) {
        this(ModEntities.HOMING_SOCK_PROJECTILE.get(), level, owner, target, damage);
    }

    protected HomingSockProjectileEntity(EntityType<? extends HomingSockProjectileEntity> entityType,
                                         Level level, LivingEntity owner, Entity target,
                                         float damage) {
        this(entityType, level);
        setOwner(owner);
        setDamage(damage);
        setTarget(target);
        entityData.set(DATA_ARC_ANGLE, level.random.nextFloat() * Mth.TWO_PI);
        entityData.set(DATA_ARC_STRENGTH, 0.65F + level.random.nextFloat() * 0.35F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TARGET_ID, 0);
        builder.define(DATA_ARC_ANGLE, 0.0F);
        builder.define(DATA_ARC_STRENGTH, 0.75F);
    }

    public void setTarget(Entity target) {
        entityData.set(DATA_TARGET_ID, target.getId());
    }

    @Override
    protected void updateFlightPath() {
        Entity target = level().getEntity(entityData.get(DATA_TARGET_ID));
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 targetPosition = target.getBoundingBox().getCenter().add(target.getDeltaMovement());
        Vec3 directionToTarget = targetPosition.subtract(position());
        if (directionToTarget.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 forward = directionToTarget.normalize();
        Vec3 referenceAxis = Math.abs(forward.y) < 0.92D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 side = forward.cross(referenceAxis).normalize();
        Vec3 vertical = side.cross(forward).normalize();
        double arcAngle = entityData.get(DATA_ARC_ANGLE);
        Vec3 arcDirection = side.scale(Math.cos(arcAngle)).add(vertical.scale(Math.sin(arcAngle)));

        int flightTicks = tickCount;
        double arcProgress = Mth.clamp(flightTicks / ARC_DURATION_TICKS, 0.0D, 1.0D);
        double arcAmount = entityData.get(DATA_ARC_STRENGTH) * (1.0D - arcProgress);
        Vec3 desiredVelocity = forward.add(arcDirection.scale(arcAmount)).normalize().scale(FLIGHT_SPEED);
        Vec3 currentVelocity = getDeltaMovement();
        if (currentVelocity.lengthSqr() < 0.01D) {
            setDeltaMovement(desiredVelocity);
        } else {
            double turnStrength = 0.18D + arcProgress * 0.42D;
            setDeltaMovement(currentVelocity.scale(1.0D - turnStrength)
                    .add(desiredVelocity.scale(turnStrength))
                    .normalize().scale(FLIGHT_SPEED));
        }

        if (flightTicks == 1 && !level().isClientSide()) {
            playSound(SoundEvents.SNOWBALL_THROW, 0.7F, 0.9F + level().random.nextFloat() * 0.2F);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity.getId() == entityData.get(DATA_TARGET_ID) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        // Homing socks phase through terrain so their locked target cannot be protected by blocks.
    }

    @Override
    protected int getLifetime() {
        return 120;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TargetId", entityData.get(DATA_TARGET_ID));
        tag.putFloat("ArcAngle", entityData.get(DATA_ARC_ANGLE));
        tag.putFloat("ArcStrength", entityData.get(DATA_ARC_STRENGTH));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_TARGET_ID, tag.getInt("TargetId"));
        entityData.set(DATA_ARC_ANGLE, tag.getFloat("ArcAngle"));
        entityData.set(DATA_ARC_STRENGTH, tag.getFloat("ArcStrength"));
    }
}
