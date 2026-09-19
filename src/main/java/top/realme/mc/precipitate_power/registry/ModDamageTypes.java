package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> BLOODI_TRUE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "bloodi_true_damage"));

    private ModDamageTypes() {
    }
}
