package top.realme.mc.precipitate_power.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractSockProjectileEntity extends Projectile {
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(AbstractSockProjectileEntity.class, EntityDataSerializers.FLOAT);

    protected AbstractSockProjectileEntity(EntityType<? extends AbstractSockProjectileEntity> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DAMAGE, 0.0F);
    }

    public void setDamage(float damage) {
        entityData.set(DATA_DAMAGE, damage);
    }

    public float getDamage() {
        return entityData.get(DATA_DAMAGE);
    }

    public void shoot(Vec3 direction, double speed) {
        setDeltaMovement(direction.normalize().scale(speed));
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > getLifetime()) {
            discard();
            return;
        }

        if (!isReadyToFly()) {
            tickBeforeLaunch();
            return;
        }

        updateFlightPath();
        HitResult hitResult = findHitResult();
        if (hitResult.getType() != HitResult.Type.MISS) {
            onHit(hitResult);
        }
        if (isRemoved()) {
            return;
        }

        Vec3 movement = getDeltaMovement();
        setPos(position().add(movement));
        updateRotationFromMovement(movement);
        if (level().isClientSide()) {
            spawnTrailParticle(movement);
        }
    }

    protected boolean isReadyToFly() {
        return true;
    }

    protected void tickBeforeLaunch() {
    }

    protected void updateFlightPath() {
    }

    protected HitResult findHitResult() {
        return ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
    }

    protected void spawnTrailParticle(Vec3 movement) {
        Vec3 trailPosition = position().subtract(movement.scale(0.35D));
        level().addParticle(ParticleTypes.CLOUD,
                trailPosition.x, trailPosition.y, trailPosition.z,
                -movement.x * 0.05D, -movement.y * 0.05D, -movement.z * 0.05D);
    }

    protected abstract int getLifetime();

    protected int getOfferingValue() {
        return 1;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        Entity owner = getOwner();
        boolean validTarget = entity instanceof LivingEntity
                || entity instanceof SockOfferingAltarEntity
                && owner instanceof Player
                && getOfferingValue() > 0;
        return validTarget
                && super.canHitEntity(entity)
                && entity != owner
                && (owner == null || !owner.isAlliedTo(entity));
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!level().isClientSide()) {
            if (hitResult.getEntity() instanceof SockOfferingAltarEntity altar
                    && getOwner() instanceof Player player
                    && getOfferingValue() > 0) {
                altar.offer(player, getOfferingValue());
                discard();
                return;
            }
            hitResult.getEntity().hurt(damageSources().thrown(this, getOwner()), getDamage());
            afterEntityHit(hitResult.getEntity());
            discard();
        }
    }

    protected void afterEntityHit(Entity target) {
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!level().isClientSide()) {
            discard();
        }
    }

    private void updateRotationFromMovement(Vec3 movement) {
        double horizontal = movement.horizontalDistance();
        setYRot((float) (Math.atan2(movement.x, movement.z) * 180.0D / Math.PI));
        setXRot((float) (Math.atan2(movement.y, horizontal) * 180.0D / Math.PI));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", getDamage());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setDamage(tag.getFloat("Damage"));
    }
}
