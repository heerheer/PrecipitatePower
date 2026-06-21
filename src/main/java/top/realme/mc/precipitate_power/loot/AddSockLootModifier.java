package top.realme.mc.precipitate_power.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModEnchantments;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.registry.ModLootModifiers;
import top.realme.mc.precipitate_power.util.SockDataUtil;

import java.util.Map;

public class AddSockLootModifier extends LootModifier {
    private static final double BOAT_SOCK_DUNGEON_CHANCE = 0.025D;
    private static final double TRAVEL_SOCK_CHEST_CHANCE = 0.018D;

    public static final MapCodec<AddSockLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).apply(inst, AddSockLootModifier::new)
    );

    public AddSockLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        String path = context.getQueriedLootTableId().getPath();

        if(!isChest(path)) {
            return generatedLoot;
        }

        if (isDungeonChest(path) && context.getRandom().nextDouble() < BOAT_SOCK_DUNGEON_CHANCE) {
            ItemStack boatSock = new ItemStack(ModItems.BOAT_SOCK.get());
            SockDataUtil.initializeBoatSock(boatSock, rollBoatSockCapacityBoost(context));
            enchantWithRandomPrideOrHumility(boatSock, context);
            generatedLoot.add(boatSock);
        }

        if (context.getRandom().nextDouble() < TRAVEL_SOCK_CHEST_CHANCE) {
            ItemStack travelSock = new ItemStack(ModItems.TRAVEL_DISPOSABLE_SOCK.get());
            enchantWithRandomPrideOrHumility(travelSock, context);
            generatedLoot.add(travelSock);
        }

        double random = context.getRandom().nextDouble();
        if (random >= Config.LOOT_SOCK_CHANCE.get()) {
            return generatedLoot;
        }

        ItemStack stack;
        if (random < 0.0061D && isNetherOrEndChest(path)) {
            stack = new ItemStack(ModItems.RAINBOW_WHITE_SOCK.get());
            SockDataUtil.initializeRainbowSock(stack);
        } else {
            stack = new ItemStack(ModItems.WHITE_SOCK.get());
        }
        if (stack.is(ModItems.WHITE_SOCK.get()) && context.getRandom().nextDouble() < Config.LOOT_BONUS_CHANCE.get()) {
            double min = Math.min(Config.LOOT_MIN_COEFFICIENT.get(), Config.LOOT_MAX_COEFFICIENT.get());
            double max = Math.max(Config.LOOT_MIN_COEFFICIENT.get(), Config.LOOT_MAX_COEFFICIENT.get());
            double coefficient = min + context.getRandom().nextDouble() * (max - min);
            SockDataUtil.setPowerCoefficient(stack, coefficient);
        }

        if (stack.is(ModItems.WHITE_SOCK.get())) {
            double athleticCognition = Math.pow(context.getRandom().nextDouble(), 2.0D);
            SockDataUtil.setAthleticCognition(stack, athleticCognition);
        }

        // 增加随机耐久附魔等级 1/2/3
        double unbreakingChance = context.getRandom().nextDouble();

        if (unbreakingChance < 0.4D) { // 40%概率附魔，不可改
            int level = 1 + context.getRandom().nextInt(3); // 随机 1~3 级

            HolderLookup.Provider lookupProvider = context.getLevel().registryAccess();
            var encUnbreaking = lookupProvider.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.UNBREAKING);
            encUnbreaking.ifPresent(enc ->{
                stack.enchant(enc.getDelegate(),level);
            });

        }

        generatedLoot.add(stack);
        return generatedLoot;
    }

    private static void enchantWithRandomPrideOrHumility(ItemStack stack, LootContext context) {
        HolderLookup.Provider lookupProvider = context.getLevel().registryAccess();
        var enchantmentLookup = lookupProvider.lookupOrThrow(Registries.ENCHANTMENT);
        var enchantment = context.getRandom().nextBoolean()
                ? enchantmentLookup.get(ModEnchantments.PRIDE)
                : enchantmentLookup.get(ModEnchantments.HUMILITY);
        int level = 1 + context.getRandom().nextInt(3);
        enchantment.ifPresent(holder -> stack.enchant(holder, level));
    }

    private static boolean isNetherOrEndChest(String path) {
        return path.equals("chests/end_city_treasure")
                || path.equals("chests/nether_bridge")
                || path.startsWith("chests/bastion_");
    }

    private static boolean isDungeonChest(String path) {
        return path.equals("chests/simple_dungeon");
    }

    private static int rollBoatSockCapacityBoost(LootContext context) {
        return 1 + (int) Math.floor(Math.pow(context.getRandom().nextDouble(), 2.0D) * 100.0D);
    }

    private static boolean isChest(String path) {
        return path.startsWith("chests/");
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_SOCK_LOOT.get();
    }
}
