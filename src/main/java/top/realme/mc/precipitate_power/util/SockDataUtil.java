package top.realme.mc.precipitate_power.util;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import top.realme.mc.precipitate_power.item.AbstractSockItem;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModItems;

/**
 * 一个对Sock进行NBT解析的辅助类
 */
public final class SockDataUtil {
    public static final double RAINBOW_POWER_COEFFICIENT = 2778.0D;
    public static final double RAINBOW_ATHLETIC_COGNITION = 1.0D;
    public static final double DIRTY_SOCK_SPEED_PENALTY = -0.10D;
    public static final double BOAT_SOCK_SPEED_PENALTY = -0.20D;
    public static final double BOAT_SOCK_PAIR_SPEED_PENALTY = -0.10D;

    // 沉淀等级
    public static final String TAG_PRECIPITATION = "PrecipitationLevel";

    // 功率系数，默认为1.0，最低为1.0
    public static final String TAG_POWER_COEFFICIENT = "PowerCoefficient";

    // 体育生认知，0.0-1.0，对应 0%-100% 速度加成
    public static final String TAG_ATHLETIC_COGNITION = "AthleticCognition";

    // 污渍等级
    public static final String TAG_DIRTY_COUNT = "DirtyCount";

    // 船袜每 20 tick 增加的机器储电量
    public static final String TAG_BOAT_SOCK_CAPACITY_BOOST = "BoatSockCapacityBoost";
    public static final String TAG_MATERIALS = "Materials";
    public static final String TAG_MATERIAL_ID = "Id";
    public static final String TAG_MATERIAL_SHARE = "Share";
    public static final String TAG_DIAMOND_BONUS_MAX = "DiamondBonusDurabilityMax";
    public static final String TAG_DIAMOND_BONUS_REMAINING = "DiamondBonusDurabilityRemaining";

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

    public static int getBoatSockCapacityBoost(ItemStack stack) {
        CompoundTag tag = getData(stack);
        return tag.contains(TAG_BOAT_SOCK_CAPACITY_BOOST) ? Math.max(1, tag.getInt(TAG_BOAT_SOCK_CAPACITY_BOOST)) : 1;
    }

    public static void setBoatSockCapacityBoost(ItemStack stack, int capacityBoost) {
        setInt(stack, TAG_BOAT_SOCK_CAPACITY_BOOST, Math.max(1, Math.min(100, capacityBoost)));
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
        return stack.getItem() instanceof AbstractSockItem;
    }

    public static boolean isWearableSock(ItemStack stack) {
        return stack.getItem() instanceof AbstractSockItem sockItem && sockItem.isWearableSock(stack);
    }

    public static boolean isUnbreakable(ItemStack stack) {
        return stack.has(DataComponents.UNBREAKABLE);
    }

    public static void initializeRainbowSock(ItemStack stack) {
        setPowerCoefficient(stack, RAINBOW_POWER_COEFFICIENT);
        setAthleticCognition(stack, RAINBOW_ATHLETIC_COGNITION);
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    public static void initializeBoatSock(ItemStack stack, int capacityBoost) {
        setBoatSockCapacityBoost(stack, capacityBoost);
    }

    public static void setMaterials(ItemStack stack, List<MaterialEntry> materials) {
        updateData(stack, tag -> {
            ListTag listTag = new ListTag();
            for (MaterialEntry entry : materials) {
                CompoundTag materialTag = new CompoundTag();
                materialTag.putString(TAG_MATERIAL_ID, entry.material().id());
                materialTag.putDouble(TAG_MATERIAL_SHARE, entry.share());
                listTag.add(materialTag);
            }
            tag.put(TAG_MATERIALS, listTag);
        });
    }

    public static List<MaterialEntry> getMaterials(ItemStack stack) {
        ListTag listTag = getData(stack).getList(TAG_MATERIALS, Tag.TAG_COMPOUND);
        return listTag.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .map(tag -> SockMaterial.byId(tag.getString(TAG_MATERIAL_ID))
                        .map(material -> new MaterialEntry(material, Math.max(0.0D, tag.getDouble(TAG_MATERIAL_SHARE))))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public static double getMaterialScalar(ItemStack stack, SockMaterial target, ToDoubleFunction<SockMaterial> extractor) {
        return getMaterials(stack).stream()
                .filter(entry -> entry.material() == target)
                .mapToDouble(entry -> extractor.applyAsDouble(entry.material()) * entry.share())
                .sum();
    }

    public static double getMaterialScalar(ItemStack stack, ToDoubleFunction<SockMaterial> extractor) {
        return getMaterials(stack).stream().mapToDouble(entry -> extractor.applyAsDouble(entry.material()) * entry.share()).sum();
    }

    public static int applyMaterialGenerationFlatBonus(ItemStack stack, int baseGeneration) {
        double flat = getMaterials(stack).stream().mapToDouble(entry -> entry.material().generationFlatBonus() * entry.share()).sum();
        return (int) Math.max(0, Math.floor(baseGeneration + flat));
    }

    public static int applyMaterialGenerationMultiplier(ItemStack stack, int baseGeneration) {
        double bonus = getMaterials(stack).stream().mapToDouble(entry -> entry.material().generationMultiplierBonus() * entry.share()).sum();
        return (int) Math.max(0, Math.floor(baseGeneration * (1.0D + bonus)));
    }

    public static void initializeDiamondDurability(ItemStack stack) {
        if (stack.isEmpty() || stack.getMaxDamage() <= 0) {
            return;
        }
        int bonus = (int) Math.round(stack.getMaxDamage() * getMaterials(stack).stream()
                .mapToDouble(entry -> entry.material().diamondDurabilityMultiplier() * entry.share())
                .sum());
        if (bonus <= 0) {
            return;
        }
        updateData(stack, tag -> {
            tag.putInt(TAG_DIAMOND_BONUS_MAX, bonus);
            tag.putInt(TAG_DIAMOND_BONUS_REMAINING, bonus);
        });
    }

    public static boolean consumeDiamondDurability(ItemStack stack, int amount) {
        CompoundTag tag = getData(stack);
        int remaining = Math.max(0, tag.getInt(TAG_DIAMOND_BONUS_REMAINING));
        if (remaining <= 0) {
            return false;
        }
        int consumed = Math.min(remaining, Math.max(1, amount));
        updateData(stack, update -> update.putInt(TAG_DIAMOND_BONUS_REMAINING, remaining - consumed));
        return true;
    }

    public static int getDiamondBonusRemaining(ItemStack stack) {
        return Math.max(0, getData(stack).getInt(TAG_DIAMOND_BONUS_REMAINING));
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

        if (!getMaterials(stack).isEmpty()) {
            tooltip.add(Component.translatable("tooltip.precipitate_power.sock.materials",
                    getMaterials(stack).stream()
                            .map(entry -> Component.translatable("material.precipitate_power." + entry.material().id())
                                    .append(" ")
                                    .append(Component.literal(formatPercent(entry.share())).withStyle(ChatFormatting.GRAY)))
                            .map(Component::getString)
                            .collect(Collectors.joining(", ")))
                    .withStyle(ChatFormatting.AQUA));
        }

        if (getDiamondBonusRemaining(stack) > 0) {
            tooltip.add(Component.translatable("tooltip.precipitate_power.sock.diamond_bonus", getDiamondBonusRemaining(stack)).withStyle(ChatFormatting.BLUE));
        }

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

    public record MaterialEntry(SockMaterial material, double share) {
    }
}
