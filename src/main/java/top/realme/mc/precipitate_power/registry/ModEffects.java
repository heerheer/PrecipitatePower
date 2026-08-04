package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.effect.LustMobEffect;
import top.realme.mc.precipitate_power.effect.FragileStomachEffect;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, PrecipitatePower.MODID);

    public static final DeferredHolder<MobEffect, LustMobEffect> LUST = REGISTER.register("lust", LustMobEffect::new);
    public static final DeferredHolder<MobEffect, FragileStomachEffect> FRAGILE_STOMACH =
            REGISTER.register("fragile_stomach", FragileStomachEffect::new);

    private ModEffects() {
    }
}
