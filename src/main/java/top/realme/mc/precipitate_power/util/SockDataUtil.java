package top.realme.mc.precipitate_power.util;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModItems;

/**
 * 一个对Sock进行NBT解析的辅助类
 */
public final class SockDataUtil {
    public static final double RAINBOW_POWER_COEFFICIENT = 2778.0D;
    public static final double RAINBOW_ATHLETIC_COGNITION = 1.0D;
    public static final double DIRTY_SOCK_SPEED_PENALTY = -0.10D;

    // 沉淀等级
    public static final String TAG_PRECIPITATION = "PrecipitationLevel";

    // 功率系数，默认为1.0，最低为1.0
    public static final String TAG_POWER_COEFFICIENT = "PowerCoefficient";

    // 体育生认知，0.0-1.0，对应 0%-100% 速度加成
    public static final String TAG_ATHLETIC_COGNITION = "AthleticCognition";

    // 污渍等级
    public static final String TAG_DIRTY_COUNT = "DirtyCount";

    private SockDataUtil() {
    }

    /**
     * 获取沉淀等级
     * @param stack
     * @return
     */
    public static int getPrecipitationLevel(ItemStack stack) {
        return getData(stack).getInt(TAG_PRECIPITATION);
    }

    /**
     * 添加沉淀等级
     * @param stack
     * @param amount
     */
    public static void addPrecipitation(ItemStack stack, int amount) {
        setInt(stack, TAG_PRECIPITATION, Math.max(0, getPrecipitationLevel(stack) + amount));
    }

    /**
     * 获取污渍等级
     * @param stack
     * @return
     */
    public static int getDirtyCount(ItemStack stack) {
        return getData(stack).getInt(TAG_DIRTY_COUNT);
    }

    /**
     * 添加污渍等级
     * @param stack
     * @param amount
     */
    public static void addDirtyCount(ItemStack stack, int amount) {
        setInt(stack, TAG_DIRTY_COUNT, Math.max(0, getDirtyCount(stack) + amount));
    }

    /**
     * 获取功率系数
     * @param stack
     * @return
     */
    public static double getPowerCoefficient(ItemStack stack) {
        CompoundTag tag = getData(stack);
        return tag.contains(TAG_POWER_COEFFICIENT) ? tag.getDouble(TAG_POWER_COEFFICIENT) : 1.0D;
    }

    /**
     * 检查是否存在功率系数
     * @param stack
     * @return
     */
    public static boolean hasPowerCoefficient(ItemStack stack) {
        return getData(stack).contains(TAG_POWER_COEFFICIENT);
    }

    /**
     * 设置功率系数，最低为1.0
     * @param stack
     * @param coefficient
     */
    public static void setPowerCoefficient(ItemStack stack, double coefficient) {
        updateData(stack, tag -> tag.putDouble(TAG_POWER_COEFFICIENT, Math.max(1.0D, coefficient)));
    }

    public static double getAthleticCognition(ItemStack stack) {
        CompoundTag tag = getData(stack);
        return tag.contains(TAG_ATHLETIC_COGNITION) ? clampPercentage(tag.getDouble(TAG_ATHLETIC_COGNITION)) : 0.0D;
    }

    public static boolean hasAthleticCognition(ItemStack stack) {
        return getData(stack).contains(TAG_ATHLETIC_COGNITION);
    }

    public static void setAthleticCognition(ItemStack stack, double cognition) {
        updateData(stack, tag -> tag.putDouble(TAG_ATHLETIC_COGNITION, clampPercentage(cognition)));
    }

    /**
     * 检查是否应该变脏
     * @param stack
     * @return
     */
    public static boolean shouldBecomeDirty(ItemStack stack) {
        return getDirtyCount(stack) >= Config.SOCK_STAIN_LIMIT.get();
    }

    public static boolean isGeneratorSock(ItemStack stack) {
        return stack.is(ModItems.WHITE_SOCK.get()) || stack.is(ModItems.RAINBOW_WHITE_SOCK.get());
    }

    public static boolean isUnbreakable(ItemStack stack) {
        return stack.has(DataComponents.UNBREAKABLE);
    }

    public static void initializeRainbowSock(ItemStack stack) {
        setPowerCoefficient(stack, RAINBOW_POWER_COEFFICIENT);
        setAthleticCognition(stack, RAINBOW_ATHLETIC_COGNITION);
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    /**
     * 在物品提示中添加沉淀等级、污渍等级和功率系数等信息
     * @param stack
     * @param tooltip
     */
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.precipitate_power.sock.precipitation", getPrecipitationLevel(stack)).withStyle(ChatFormatting.AQUA));

        if (!stack.has(DataComponents.UNBREAKABLE) && getDirtyCount(stack) > 0) // 污渍等级的显示优化
            tooltip.add(Component.translatable("tooltip.precipitate_power.sock.dirty_count", getDirtyCount(stack), Config.SOCK_STAIN_LIMIT.get()).withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("tooltip.precipitate_power.sock.power_coefficient", formatDecimal(getPowerCoefficient(stack))).withStyle(ChatFormatting.GOLD));
        
        tooltip.add(Component.translatable("tooltip.precipitate_power.sock.athletic_cognition", formatPercent(getAthleticCognition(stack))).withStyle(ChatFormatting.GOLD));
        
        if (isUnbreakable(stack)) {
            tooltip.add(Component.translatable("tooltip.precipitate_power.sock.unbreakable").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static double clampPercentage(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", clampPercentage(value) * 100.0D);
    }

    private static CompoundTag getData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setInt(ItemStack stack, String key, int value) {
        updateData(stack, tag -> tag.putInt(key, value));
    }

    private static void updateData(ItemStack stack, java.util.function.Consumer<CompoundTag> consumer) {
        CustomData updated = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(consumer);
        stack.set(DataComponents.CUSTOM_DATA, updated);
    }
}
