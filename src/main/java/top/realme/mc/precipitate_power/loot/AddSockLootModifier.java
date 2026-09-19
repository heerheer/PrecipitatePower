package top.realme.mc.precipitate_power.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.item.SockMaterialRoller;
import top.realme.mc.precipitate_power.registry.ModEnchantments;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.registry.ModLootModifiers;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class AddSockLootModifier extends LootModifier {
    private static final double ORIGINAL_SCENT_CHEST_CHANCE = 0.01D;
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

        if (!isChest(path)) {
            return generatedLoot;
        }

        if (path.equals("chests/end_city_treasure") && context.getRandom().nextDouble() < ORIGINAL_SCENT_CHEST_CHANCE) {
            ItemStack scent = new ItemStack(ModItems.ZHAOZHAO_ORIGINAL_SCENT.get());
            enchantFixedPrideAndHumility(scent, context);
            generatedLoot.add(scent);
        }

        if (isVillageChest(path) && context.getRandom().nextDouble() < ORIGINAL_SCENT_CHEST_CHANCE) {
            generatedLoot.add(new ItemStack(ModItems.FUSHENG_ORIGINAL_SCENT.get()));
        }

        if (path.equals("chests/nether_bridge") && context.getRandom().nextDouble() < ORIGINAL_SCENT_CHEST_CHANCE) {
            generatedLoot.add(new ItemStack(ModItems.DATOU_ORIGINAL_SCENT.get()));
        }

        if (isDungeonChest(path) && context.getRandom().nextDouble() < BOAT_SOCK_DUNGEON_CHANCE) {
            ItemStack boatSock = new ItemStack(ModItems.BOAT_SOCK.get());
            SockDataUtil.initializeBoatSock(boatSock, rollBoatSockCapacityBoost(context));
            SockMaterialRoller.initializeRolledSock(boatSock, context.getRandom());
            enchantWithRandomPrideOrHumility(boatSock, context);
            generatedLoot.add(boatSock);
        }

        if (context.getRandom().nextDouble() < TRAVEL_SOCK_CHEST_CHANCE) {
            ItemStack travelSock = new ItemStack(ModItems.TRAVEL_DISPOSABLE_SOCK.get());
            SockMaterialRoller.initializeRolledSock(travelSock, context.getRandom());
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
            stack = rollBaseSock(context);
        }
        SockMaterialRoller.initializeRolledSock(stack, context.getRandom());

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

    private static ItemStack rollBaseSock(LootContext context) {
        int roll = context.getRandom().nextInt(6);
        return switch (roll) {
            case 1 -> new ItemStack(ModItems.OVER_KNEE_SOCK.get());
            case 2 -> new ItemStack(ModItems.SPORT_CREW_SOCK.get());
            case 3 -> new ItemStack(ModItems.PANTYHOSE.get());
            case 4 -> new ItemStack(ModItems.SPLIT_TOE_SOCK.get());
            case 5 -> new ItemStack(ModItems.STOCKINGS.get());
            default -> new ItemStack(ModItems.WHITE_SOCK.get());
        };
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

    private static void enchantFixedPrideAndHumility(ItemStack stack, LootContext context) {
        HolderLookup.Provider lookupProvider = context.getLevel().registryAccess();
        var enchantmentLookup = lookupProvider.lookupOrThrow(Registries.ENCHANTMENT);
        enchantmentLookup.get(ModEnchantments.PRIDE).ifPresent(holder -> stack.enchant(holder, 3));
        enchantmentLookup.get(ModEnchantments.HUMILITY).ifPresent(holder -> stack.enchant(holder, 3));
    }

    private static boolean isNetherOrEndChest(String path) {
        return path.equals("chests/end_city_treasure")
                || path.equals("chests/nether_bridge")
                || path.startsWith("chests/bastion_");
    }

    private static boolean isDungeonChest(String path) {
        return path.equals("chests/simple_dungeon");
    }

    private static boolean isVillageChest(String path) {
        return path.startsWith("chests/village/");
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
