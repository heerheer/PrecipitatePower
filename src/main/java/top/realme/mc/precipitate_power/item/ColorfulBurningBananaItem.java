package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.block.entity.AbstractPrecipitateGeneratorBlockEntity;

public final class ColorfulBurningBananaItem extends BurningBananaItem {
    public static final int MAX_HEALTH_BONUS = 10;
    private static final double FIXED_BREAK_CHANCE = 0.001D;
    private static final String HEALTH_BONUS_TAG = "ColorfulBurningBananaHealthBonus";
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            PrecipitatePower.MODID, "colorful_burning_banana_health");

    public ColorfulBurningBananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onInsertedIntoGenerator(AbstractPrecipitateGeneratorBlockEntity generator, ItemStack stack) {
        generator.fillEnergyToCapacity();
    }

    @Override
    protected double breakChanceAfterBurst(int explosionCount) {
        return FIXED_BREAK_CHANCE;
    }

    @Override
    protected boolean canCauseFragileStomach() {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (getHealthBonus(player) >= MAX_HEALTH_BONUS) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable(
                        "message.precipitate_power.colorful_burning_banana.health_max"), true);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (level instanceof ServerLevel && livingEntity instanceof Player player) {
            int oldBonus = getHealthBonus(player);
            if (oldBonus < MAX_HEALTH_BONUS) {
                int newBonus = oldBonus + 1;
                setHealthBonus(player, newBonus);
                applyHealthBonus(player);
                player.heal(1.0F);
                player.displayClientMessage(Component.translatable(
                        "message.precipitate_power.colorful_burning_banana.health_gained",
                        newBonus, MAX_HEALTH_BONUS), true);
            }
        }
        return result;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 64;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.precipitate_power.colorful_burning_banana.instant_charge")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.precipitate_power.colorful_burning_banana.health", MAX_HEALTH_BONUS)
                .withStyle(ChatFormatting.RED));
    }

    public static int getHealthBonus(Player player) {
        return Math.max(0, Math.min(MAX_HEALTH_BONUS,
                getPersistedData(player).getInt(HEALTH_BONUS_TAG)));
    }

    public static void applyHealthBonus(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        int bonus = getHealthBonus(player);
        if (bonus <= 0) {
            maxHealth.removeModifier(HEALTH_MODIFIER_ID);
            return;
        }
        maxHealth.addOrReplacePermanentModifier(new AttributeModifier(
                HEALTH_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void setHealthBonus(Player player, int bonus) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag persisted = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(HEALTH_BONUS_TAG, Math.max(0, Math.min(MAX_HEALTH_BONUS, bonus)));
        persistent.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    private static CompoundTag getPersistedData(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
