package top.realme.mc.precipitate_power.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public final class SockMaterialRoller {
    private static final List<SockMaterial> COMMON_POOL = SockMaterial.VALUES.stream().filter(material -> material.poolWeight() >= 0.70D).toList();
    private static final List<SockMaterial> VANILLA_POOL = SockMaterial.VALUES.stream().filter(material -> material.poolWeight() < 0.70D).toList();

    private SockMaterialRoller() {
    }

    public static void initializeRolledSock(ItemStack stack, RandomSource random) {
        if (!(stack.getItem() instanceof AbstractSockItem sockItem) || !sockItem.rollMaterialsOnGeneration()) {
            return;
        }

        if (stack.is(ModItems.WHITE_SOCK.get())
                && random.nextDouble() < Config.LOOT_BONUS_CHANCE.get()) {
            double min = Math.min(Config.LOOT_MIN_COEFFICIENT.get(), Config.LOOT_MAX_COEFFICIENT.get());
            double max = Math.max(Config.LOOT_MIN_COEFFICIENT.get(), Config.LOOT_MAX_COEFFICIENT.get());
            SockDataUtil.setPowerCoefficient(stack, min + random.nextDouble() * (max - min));
        }

        if (stack.is(ModItems.WHITE_SOCK.get())
                || stack.is(ModItems.OVER_KNEE_SOCK.get())
                || stack.is(ModItems.SPORT_CREW_SOCK.get())
                || stack.is(ModItems.PANTYHOSE.get())
                || stack.is(ModItems.SPLIT_TOE_SOCK.get())
                || stack.is(ModItems.STOCKINGS.get())) {
            SockDataUtil.setAthleticCognition(stack, Math.pow(random.nextDouble(), 2.0D));
        }

        int count = 1 + random.nextInt(5);
        List<SockDataUtil.MaterialEntry> materials = new ArrayList<>();
        List<SockMaterial> availableCommon = new ArrayList<>(COMMON_POOL);
        List<SockMaterial> availableVanilla = new ArrayList<>(VANILLA_POOL);

        double totalWeight = 0.0D;
        for (int i = 0; i < count; i++) {
            boolean pickVanilla = !availableVanilla.isEmpty() && (availableCommon.isEmpty() || random.nextDouble() >= 0.70D);
            List<SockMaterial> pool = pickVanilla ? availableVanilla : availableCommon;
            SockMaterial material = pool.remove(random.nextInt(pool.size()));
            double weight = 0.25D + random.nextDouble();
            totalWeight += weight;
            materials.add(new SockDataUtil.MaterialEntry(material, weight));
        }

        if (totalWeight <= 0.0D) {
            return;
        }

        final double finalTotalWeight = totalWeight;
        List<SockDataUtil.MaterialEntry> normalized = materials.stream()
                .map(entry -> new SockDataUtil.MaterialEntry(entry.material(), entry.share() / finalTotalWeight))
                .sorted(Comparator.comparing(entry -> entry.material().id()))
                .toList();

        SockDataUtil.setMaterials(stack, normalized);
        SockDataUtil.initializeDiamondDurability(stack);
    }
}
