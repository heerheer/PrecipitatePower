package top.realme.mc.precipitate_power.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.registry.ModAdvancements;
import top.realme.mc.precipitate_power.registry.ModEffects;

public class ZhaozhaoOriginalScentItem extends OriginalScentItem {
    private static final int COOLDOWN_TICKS = 20 * 120;
    private static final int EAT_DURATION_TICKS = 32;

    public ZhaozhaoOriginalScentItem(Properties properties) {
        super(properties, "tooltip.precipitate_power.zhaozhao_original_scent", "Research_King");
    }

    @Override
    public ItemStack getDefaultInstance() {
        return super.getDefaultInstance();
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        double centerX = context.pos().getX() + 0.5D;
        double centerY = context.pos().getY() + 0.8D;
        double centerZ = context.pos().getZ() + 0.5D;

        if (context.consumeStoredEnergy(40)) {
            if (context.level().getGameTime() % 20L == 0L) {
                for (LivingEntity livingEntity : context.findNearbyLivingEntities(6.0D)) {
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.LUST, 60, 0, false, true, true));
                }
                context.spawnParticles(ParticleTypes.HEART, centerX, centerY, centerZ, 8, 0.5D, 0.4D, 0.5D, 0.02D);
            } else {
                context.spawnParticles(ParticleTypes.CHERRY_LEAVES, centerX, centerY, centerZ, 1, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        } else {
            context.spawnParticles(ParticleTypes.HEART, centerX, centerY, centerZ, 1, 0.1D, 0.1D, 0.1D, 0.0D);
        }
        return GeneratorTickResult.handled(0, 0, false, context.inputStack(), ItemStack.EMPTY);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return EAT_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }

        if (!level.isClientSide) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 8 * 20, 1));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 15 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 15 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 23 * 20, 4));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30 * 20, 1));
            player.playSound(SoundEvents.GENERIC_EAT, 1.0F, 0.8F);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancements.grant(serverPlayer, ModAdvancements.THE_REAL_TASTE);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return stack;
    }
}
