package top.realme.mc.precipitate_power.compat.immortalersdelight;

import com.renyigesai.immortalers_delight.init.ImmortalersDelightItems;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.realme.mc.precipitate_power.compat.curios.CuriosCompat;
import top.realme.mc.precipitate_power.item.SockMaterial;

public final class ImmortalersDelightCompat {
    private static final String TAG_BARTER_PLAYER = "PrecipitatePowerGoldenFabricPlayer";
    private static final String TAG_BARTER_EXPERIENCE = "PrecipitatePowerGoldenFabricExperience";
    private static final List<DeferredHolder<Item, Item>> SNIFFER_SEEDS = List.of(
            ImmortalersDelightItems.EVOLUTCORN_GRAINS,
            ImmortalersDelightItems.PEARLIPEARL,
            ImmortalersDelightItems.HIMEKAIDO_SEED,
            ImmortalersDelightItems.CONTAINS_TEA_LEISAMBOO,
            ImmortalersDelightItems.KWAT_WHEAT_SEEDS,
            ImmortalersDelightItems.ALFALFA_SEEDS,
            ImmortalersDelightItems.WARPED_LAUREL_SEEDS,
            ImmortalersDelightItems.TRAVARICE,
            ImmortalersDelightItems.GELPITAYA_SEEDS,
            ImmortalersDelightItems.A_BUSH,
            ImmortalersDelightItems.OXYGRAPE,
            ImmortalersDelightItems.SEXTLOTUS_SEEDS
    );

    private ImmortalersDelightCompat() {
    }

    public static void dropRandomSnifferSeed(ServerPlayer player) {
        Item seed = SNIFFER_SEEDS.get(player.getRandom().nextInt(SNIFFER_SEEDS.size())).get();
        player.spawnAtLocation(new ItemStack(seed));
    }

    public static void tickPiglinBarter(Piglin piglin) {
        if (!(piglin.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag data = piglin.getPersistentData();
        if (piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
            if (!data.contains(TAG_BARTER_PLAYER)) {
                findGoldenFabricTrader(serverLevel, piglin).ifPresent(player -> {
                    data.putUUID(TAG_BARTER_PLAYER, player.getUUID());
                    data.putInt(TAG_BARTER_EXPERIENCE, getBarterExperience(player));
                });
            }
            return;
        }

        if (!data.hasUUID(TAG_BARTER_PLAYER)) {
            return;
        }

        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(data.getUUID(TAG_BARTER_PLAYER));
        if (player != null) {
            player.giveExperiencePoints(data.getInt(TAG_BARTER_EXPERIENCE));
        }
        data.remove(TAG_BARTER_PLAYER);
        data.remove(TAG_BARTER_EXPERIENCE);
    }

    private static java.util.Optional<ServerPlayer> findGoldenFabricTrader(ServerLevel level, Piglin piglin) {
        return level.players().stream()
                .filter(player -> player.distanceToSqr(piglin) <= 256.0D)
                .filter(player -> getGoldenFabricScalar(player) > 0.0D)
                .min(Comparator.comparingDouble(player -> player.distanceToSqr(piglin)));
    }

    private static int getBarterExperience(ServerPlayer player) {
        return Math.max(1, (int) Math.ceil(getGoldenFabricScalar(player) * 3.0D));
    }

    private static double getGoldenFabricScalar(ServerPlayer player) {
        return CuriosCompat.getEquippedMaterialScalar(player, SockMaterial.GOLDEN_FABRIC, material -> 1.0D);
    }
}
