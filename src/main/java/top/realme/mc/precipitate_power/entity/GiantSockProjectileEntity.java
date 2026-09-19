package top.realme.mc.precipitate_power.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.realme.mc.precipitate_power.registry.ModEntities;

import java.util.Optional;

public class GiantSockProjectileEntity extends AbstractSockProjectileEntity {
    private float collisionScale = 1.0F;

    public GiantSockProjectileEntity(EntityType<? extends GiantSockProjectileEntity> entityType, Level level) {
        super(entityType, level);
        refreshDimensions();
    }

    public GiantSockProjectileEntity(Level level, LivingEntity owner, float damage, float collisionScale) {
        this(ModEntities.GIANT_SOCK_PROJECTILE.get(), level);
        setOwner(owner);
        setDamage(damage);
        setCollisionScale(collisionScale);
    }

    public void setCollisionScale(float collisionScale) {
        this.collisionScale = Math.max(0.1F, collisionScale);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(Math.max(0.1F, collisionScale));
    }

    @Override
    protected HitResult findHitResult() {
        Vec3 start = position();
        Vec3 end = start.add(getDeltaMovement());
        BlockHitResult blockHit = level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        double nearestDistance = blockHit.getType() == HitResult.Type.MISS
                ? start.distanceToSqr(end)
                : start.distanceToSqr(blockHit.getLocation());

        double halfSize = getBbWidth() * 0.5D;
        AABB sweptVolume = centeredCollisionBox(start, halfSize)
                .minmax(centeredCollisionBox(end, halfSize));
        EntityHitResult nearestEntityHit = null;

        for (net.minecraft.world.entity.Entity entity
                : level().getEntities(this, sweptVolume, this::canHitEntity)) {
            AABB expandedTarget = entity.getBoundingBox().inflate(halfSize);
            Optional<Vec3> clipped = expandedTarget.clip(start, end);
            Vec3 hitLocation = expandedTarget.contains(start) ? start : clipped.orElse(null);
            if (hitLocation == null) {
                continue;
            }
            double hitDistance = start.distanceToSqr(hitLocation);
            if (hitDistance < nearestDistance) {
                nearestDistance = hitDistance;
                nearestEntityHit = new EntityHitResult(entity, hitLocation);
            }
        }
        return nearestEntityHit != null ? nearestEntityHit : blockHit;
    }

    private static AABB centeredCollisionBox(Vec3 center, double halfSize) {
        return new AABB(
                center.x - halfSize, center.y - halfSize, center.z - halfSize,
                center.x + halfSize, center.y + halfSize, center.z + halfSize);
    }

    @Override
    protected int getOfferingValue() {
        return 2;
    }

    @Override
    protected int getLifetime() {
        return 120;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("CollisionScale", collisionScale);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCollisionScale(tag.contains("CollisionScale") ? tag.getFloat("CollisionScale") : 1.0F);
    }
}
