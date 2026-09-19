package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.compat.curios.CuriosCompat;
import top.realme.mc.precipitate_power.registry.ModEnchantments;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.FormulaParser;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public abstract class AbstractSockItem extends Item implements GeneratorFuelItem {
    protected static final double ATHLETIC_COGNITION_LOSS_CHANCE = 0.5D;
    protected static final double ATHLETIC_COGNITION_LOSS_AMOUNT = 0.01D;

    protected AbstractSockItem(Properties properties) {
        super(properties);
    }

    public boolean isWearableSock(ItemStack stack) {
        return true;
    }

    public boolean rollMaterialsOnGeneration() {
        return true;
    }

    public boolean supportsMaterialBlending(ItemStack stack) {
        return rollMaterialsOnGeneration();
    }

    @Override
    public boolean canPrecipitateInGenerator(ItemStack stack) {
        return true;
    }

    @Override
    public int applyPrecipitationRolls(GeneratorTickContext context, long attempts) {
        ItemStack stack = context.inputStack();
        int currentLevel = SockDataUtil.getPrecipitationLevel(stack);
        long availableLevels = (long) Integer.MAX_VALUE - currentLevel;
        if (attempts <= 0L || availableLevels <= 0L) {
            return 0;
        }

        long successes = rollIndependentPrecipitationAttempts(
                context.random(), attempts, Config.PRECIPITATE_CHANCE.get(), availableLevels);
        int addedLevels = (int) Math.min(availableLevels, successes);
        if (addedLevels > 0) {
            SockDataUtil.addPrecipitation(stack, addedLevels);
        }
        return addedLevels;
    }

    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        return tickAsNormalSock(context);
    }

    protected GeneratorTickResult tickAsNormalSock(GeneratorTickContext context) {
        ItemStack stack = context.inputStack();
        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        int generated = calculateBaseGeneration(context, precipitation);
        generated = SockDataUtil.applyMaterialGenerationFlatBonus(stack, generated);
        generated = SockDataUtil.applyMaterialGenerationMultiplier(stack, generated);
        boolean changed = false;

        if (generated > 0 && context.canConsumeGenerationResource(precipitation)) {
            context.consumeGenerationResource(precipitation);
            changed = true;
        } else {
            generated = 0;
        }

        if (context.level().getGameTime() % 20L == 0L) {
            changed |= applyPrecipitationRolls(context, 1L) > 0;
        }

        if (applyDirtyLogic(context, precipitation)) {
            changed = true;
        }

        return GeneratorTickResult.handled(generated, 0, changed, stack, ItemStack.EMPTY);
    }

    private static long rollIndependentPrecipitationAttempts(
            RandomSource random, long attempts, double chance, long maximumSuccesses) {
        if (attempts <= 0L || maximumSuccesses <= 0L || chance <= 0.0D) {
            return 0L;
        }
        if (chance >= 1.0D) {
            return Math.min(attempts, maximumSuccesses);
        }

        double logFailureChance = Math.log1p(-chance);
        long remainingAttempts = attempts;
        long successes = 0L;
        while (remainingAttempts > 0L && successes < maximumSuccesses) {
            double roll = random.nextDouble();
            double sampledFailures = Math.floor(Math.log1p(-roll) / logFailureChance);
            if (sampledFailures >= remainingAttempts || sampledFailures >= Long.MAX_VALUE) {
                break;
            }

            long failuresBeforeSuccess = (long) sampledFailures;
            successes++;
            remainingAttempts -= failuresBeforeSuccess + 1L;
        }
        return successes;
    }

    protected int calculateBaseGeneration(GeneratorTickContext context, int precipitation) {
        ItemStack stack = context.inputStack();
        double coefficient = SockDataUtil.getPowerCoefficient(stack);
        double baseGeneration = FormulaParser.evaluate(Config.GENERATION_FORMULA.get(), precipitation);
        return (int) Math.max(0, Math.floor(baseGeneration * coefficient * context.generationMultiplier()));
    }

    protected boolean applyDirtyLogic(GeneratorTickContext context, int precipitation) {
        ItemStack stack = context.inputStack();
        if (SockDataUtil.isUnbreakable(stack)) {
            return false;
        }

        double dirtyChance = Config.DIRTY_BASE_CHANCE.get() + precipitation * Config.DIRTY_CHANCE_PER_PRECIPITATION.get();
        dirtyChance *= context.dirtyChanceMultiplier();
        if (context.random().nextDouble() >= dirtyChance) {
            return false;
        }

        if (SockDataUtil.getAthleticCognition(stack) > 0.0D && context.random().nextDouble() < ATHLETIC_COGNITION_LOSS_CHANCE) {
            SockDataUtil.setAthleticCognition(stack, SockDataUtil.getAthleticCognition(stack) - ATHLETIC_COGNITION_LOSS_AMOUNT);
            return true;
        }

        SockDataUtil.addDirtyCount(stack, 1);
        if (SockDataUtil.shouldBecomeDirty(stack)) {
            context.generator().replaceInputWithDirtySock();
        }
        return true;
    }

    protected int consumeDurabilityWithModifiers(ItemStack stack, ServerLevel serverLevel) {
        int humility = getSockEnchantmentLevel(stack, serverLevel, ModEnchantments.HUMILITY);
        if (humility > 0 && serverLevel.random.nextDouble() < Math.min(0.99D, 0.33D * Math.min(3, humility))) {
            return 0;
        }

        double nylonChance = SockDataUtil.getMaterialScalar(stack, SockMaterial.NYLON, SockMaterial::nylonNoDamageChance);
        if (nylonChance > 0.0D && serverLevel.random.nextDouble() < nylonChance) {
            return 0;
        }

        int multiplier = getPrideDurabilityMultiplier(stack, serverLevel);
        if (!SockDataUtil.consumeDiamondDurability(stack, multiplier)) {
            stack.hurtAndBreak(1, serverLevel, null, item -> {
            });
        }

        double repairChance = SockDataUtil.getMaterialScalar(stack, SockMaterial.BAMBOO, SockMaterial::repairChance);
        if (repairChance > 0.0D && stack.isDamaged() && serverLevel.random.nextDouble() < repairChance) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        }
        return multiplier;
    }

    protected int getSockEnchantmentLevel(ItemStack stack, ServerLevel serverLevel, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return stack.getEnchantmentLevel(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment));
    }

    protected int getPrideDurabilityMultiplier(ItemStack stack, ServerLevel serverLevel) {
        int pride = Math.min(3, getSockEnchantmentLevel(stack, serverLevel, ModEnchantments.PRIDE));
        int multiplier = 1;
        for (int i = 0; i < pride; i++) {
            multiplier *= 6;
        }
        return multiplier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        SockDataUtil.appendTooltip(stack, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }
}
