package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.ChesedUpgradeEntity;
import top.realme.mc.precipitate_power.entity.BloodiPowerAreaEntity;
import top.realme.mc.precipitate_power.entity.BloodiProjectileEntity;
import top.realme.mc.precipitate_power.entity.GiantSockProjectileEntity;
import top.realme.mc.precipitate_power.entity.HomingSockProjectileEntity;
import top.realme.mc.precipitate_power.entity.SockOfferingAltarEntity;
import top.realme.mc.precipitate_power.entity.SockOfferingProjectileEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(Registries.ENTITY_TYPE, PrecipitatePower.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ChesedUpgradeEntity>> CHESED_UPGRADE = REGISTER.register(
            "chesed_upgrade",
            () -> EntityType.Builder.of(ChesedUpgradeEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("chesed_upgrade")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<GiantSockProjectileEntity>> GIANT_SOCK_PROJECTILE = REGISTER.register(
            "giant_sock_projectile",
            () -> EntityType.Builder.<GiantSockProjectileEntity>of(GiantSockProjectileEntity::new, MobCategory.MISC)
                    .sized(3.0F, 3.0F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("giant_sock_projectile")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<HomingSockProjectileEntity>> HOMING_SOCK_PROJECTILE = REGISTER.register(
            "homing_sock_projectile",
            () -> EntityType.Builder.<HomingSockProjectileEntity>of(HomingSockProjectileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("homing_sock_projectile")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BloodiProjectileEntity>> BLOODI_PROJECTILE = REGISTER.register(
            "bloodi_projectile",
            () -> EntityType.Builder.<BloodiProjectileEntity>of(BloodiProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("bloodi_projectile")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BloodiPowerAreaEntity>> BLOODI_POWER_AREA = REGISTER.register(
            "bloodi_power_area",
            () -> EntityType.Builder.<BloodiPowerAreaEntity>of(BloodiPowerAreaEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("bloodi_power_area")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SockOfferingAltarEntity>> SOCK_OFFERING_ALTAR = REGISTER.register(
            "sock_offering_altar",
            () -> EntityType.Builder.<SockOfferingAltarEntity>of(SockOfferingAltarEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("sock_offering_altar")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SockOfferingProjectileEntity>> SOCK_OFFERING_PROJECTILE = REGISTER.register(
            "sock_offering_projectile",
            () -> EntityType.Builder.<SockOfferingProjectileEntity>of(SockOfferingProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("sock_offering_projectile")
    );

    private ModEntities() {
    }
}
