package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.ChesedUpgradeEntity;

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

    private ModEntities() {
    }
}
