package top.realme.mc.precipitate_power.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ColorfulBananaSliceItem extends Item {
    public ColorfulBananaSliceItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (level instanceof ServerLevel) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 4));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 4));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 60 * 2, 4));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 2));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60 * 5, 0));
        }
        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
