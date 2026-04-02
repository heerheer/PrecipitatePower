package top.realme.mc.precipitate_power.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.registry.ModLootModifiers;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class AddSockLootModifier extends LootModifier {
    public static final MapCodec<AddSockLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).apply(inst, AddSockLootModifier::new)
    );

    public AddSockLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        String path = context.getQueriedLootTableId().getPath();
        if (!isNetherOrEndChest(path)) {
            return generatedLoot;
        }
        if (context.getRandom().nextDouble() >= Config.LOOT_SOCK_CHANCE.get()) {
            return generatedLoot;
        }

        ItemStack stack;
        if (context.getRandom().nextDouble() < 0.08D) {
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
        generatedLoot.add(stack);
        return generatedLoot;
    }

    private static boolean isNetherOrEndChest(String path) {
        return path.equals("chests/end_city_treasure")
                || path.equals("chests/nether_bridge")
                || path.startsWith("chests/bastion_");
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_SOCK_LOOT.get();
    }
}
