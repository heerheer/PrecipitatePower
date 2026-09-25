package top.realme.mc.precipitate_power.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class LustMobEffect extends MobEffect {
    public LustMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8FCF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Mob mob) {
            mob.setTarget(null);
        }
        return true;
    }
}
