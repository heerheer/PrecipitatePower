package top.realme.mc.precipitate_power.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.registry.ModEntities;

public class SockOfferingProjectileEntity extends HomingSockProjectileEntity {
    public SockOfferingProjectileEntity(EntityType<? extends SockOfferingProjectileEntity> entityType,
                                        Level level) {
        super(entityType, level);
    }

    public SockOfferingProjectileEntity(Level level, LivingEntity owner, LivingEntity target,
                                        float damage) {
        super(ModEntities.SOCK_OFFERING_PROJECTILE.get(), level, owner, target, damage);
    }

    @Override
    protected int getOfferingValue() {
        return 0;
    }
}
