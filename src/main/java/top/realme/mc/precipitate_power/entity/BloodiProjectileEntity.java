package top.realme.mc.precipitate_power.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import top.realme.mc.precipitate_power.registry.ModDamageTypes;
import top.realme.mc.precipitate_power.registry.ModEntities;

public class BloodiProjectileEntity extends HomingSockProjectileEntity {
    public BloodiProjectileEntity(EntityType<? extends BloodiProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BloodiProjectileEntity(Level level, LivingEntity owner, LivingEntity target,
                                  float damage) {
        super(ModEntities.BLOODI_PROJECTILE.get(), level, owner, target, damage);
    }

    @Override
    protected int getOfferingValue() {
        return 0;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!level().isClientSide()) {
            hitResult.getEntity().hurt(
                    damageSources().source(ModDamageTypes.BLOODI_TRUE_DAMAGE, this, getOwner()),
                    getDamage());
            discard();
        }
    }
}
