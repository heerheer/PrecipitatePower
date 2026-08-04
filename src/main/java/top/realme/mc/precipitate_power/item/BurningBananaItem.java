package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModDataComponents;
import top.realme.mc.precipitate_power.util.FormulaParser;
import top.realme.mc.precipitate_power.util.SockDataUtil;
import top.realme.mc.precipitate_power.registry.ModEffects;

public class BurningBananaItem extends Item implements GeneratorFuelItem {
    public static final int HEAT_INTERVAL_TICKS = 20 * 5;
    public static final int HEAT_REQUIRED_FOR_BURST = 5;
    private static final double NORMAL_BREAK_CHANCE_PER_EXPLOSION = 0.01D;
    private static final double FRAGILE_STOMACH_CHANCE = 0.25D;
    private static final int FRAGILE_STOMACH_DURATION_TICKS = 20 * 60 * 5;

    public BurningBananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, net.minecraft.world.level.Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (canCauseFragileStomach() && level instanceof ServerLevel serverLevel
                && serverLevel.random.nextDouble() < FRAGILE_STOMACH_CHANCE) {
            livingEntity.addEffect(new MobEffectInstance(
                    ModEffects.FRAGILE_STOMACH, FRAGILE_STOMACH_DURATION_TICKS, 0));
        }
        return result;
    }

    protected boolean canCauseFragileStomach() {
        return true;
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        ItemStack stack = context.inputStack();
        BurningBananaData data = getData(stack);
        boolean changed = false;

        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        int generated = (int) Math.max(0, Math.floor(
                FormulaParser.evaluate(Config.GENERATION_FORMULA.get(), precipitation)
                        * context.generationMultiplier()));
        boolean activelyGenerating = generated > 0 && context.canConsumeGenerationResource(precipitation);
        if (activelyGenerating) {
            context.consumeGenerationResource(precipitation);
            changed = true;
        } else {
            generated = 0;
        }

        long gameTime = context.level().getGameTime();
        if (gameTime % 20L == 0L && context.random().nextDouble() < Config.PRECIPITATE_CHANCE.get()) {
            SockDataUtil.addPrecipitation(stack, 1);
            changed = true;
        }

        if (activelyGenerating && gameTime % HEAT_INTERVAL_TICKS == 0L) {
            int nextHeat = data.heat() + 1;
            if (nextHeat < HEAT_REQUIRED_FOR_BURST) {
                data = data.withHeat(nextHeat);
            } else {
                data = data.afterBurst();
                generated = (int) Math.min(Integer.MAX_VALUE,
                        (long) generated + Math.max(1, context.energyCapacity() / 4));
                playBurstEffects(context);
                if (context.random().nextDouble() < breakChanceAfterBurst(data.explosions())) {
                    setData(stack, data);
                    return GeneratorTickResult.handled(generated, 0, true, ItemStack.EMPTY, ItemStack.EMPTY);
                }
            }
            changed = true;
        }

        if (changed) {
            setData(stack, data);
        }
        return GeneratorTickResult.handled(generated, 0, changed, stack, ItemStack.EMPTY);
    }

    protected double breakChanceAfterBurst(int explosionCount) {
        return Math.min(1.0D, explosionCount * NORMAL_BREAK_CHANCE_PER_EXPLOSION);
    }

    protected void playBurstEffects(GeneratorTickContext context) {
        context.spawnParticles(ParticleTypes.EXPLOSION,
                context.pos().getX() + 0.5D, context.pos().getY() + 1.0D, context.pos().getZ() + 0.5D,
                1, 0.05D, 0.05D, 0.05D, 0.0D);
        context.spawnParticles(ParticleTypes.FLAME,
                context.pos().getX() + 0.5D, context.pos().getY() + 0.8D, context.pos().getZ() + 0.5D,
                12, 0.25D, 0.2D, 0.25D, 0.02D);
        context.level().playSound(null, context.pos(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.7F, 1.35F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BurningBananaData data = getData(stack);
        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        tooltip.add(Component.translatable("tooltip.precipitate_power.burning_banana.precipitation", precipitation)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.precipitate_power.burning_banana.heat",
                data.heat(), HEAT_REQUIRED_FOR_BURST).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.precipitate_power.burning_banana.explosions", data.explosions())
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.precipitate_power.burning_banana.break_chance",
                String.format(java.util.Locale.ROOT, "%.1f", breakChanceAfterBurst(data.explosions() + 1) * 100.0D))
                .withStyle(ChatFormatting.DARK_RED));
        if (canCauseFragileStomach()) {
            tooltip.add(Component.translatable("tooltip.precipitate_power.burning_banana.fragile_stomach")
                    .withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static BurningBananaData getData(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BURNING_BANANA_DATA.get(), BurningBananaData.DEFAULT);
    }

    public static void setData(ItemStack stack, BurningBananaData data) {
        stack.set(ModDataComponents.BURNING_BANANA_DATA.get(), data);
    }
}
