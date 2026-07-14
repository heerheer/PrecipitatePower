package top.realme.mc.precipitate_power.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.compat.curios.CuriosCompat;
import top.realme.mc.precipitate_power.compat.immortalersdelight.ImmortalersDelightCompat;
import top.realme.mc.precipitate_power.item.FushengOriginalScentItem;
import top.realme.mc.precipitate_power.item.OriginalScentItem;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.registry.ModAdvancements;
import top.realme.mc.precipitate_power.registry.ModEffects;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.SockDataUtil;

@EventBusSubscriber(modid = PrecipitatePower.MODID)
public final class ModEvents {
    private static final String LEGACY_TAG_SNIFFER_FUR_DISTANCE = "PrecipitatePowerSnifferFurDistance";
    private static final String TAG_SNIFFER_FUR_LAST_X = "PrecipitatePowerSnifferFurLastX";
    private static final String TAG_SNIFFER_FUR_LAST_Z = "PrecipitatePowerSnifferFurLastZ";
    private static final double MAX_SNIFFER_FUR_DISTANCE_PER_TICK = 2.0D;

    private ModEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (player.tickCount % 20 == 0 && hasFiveMaterialSock(serverPlayer)) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.ULTIMATE_BLENDER);
        }

        double jumpBonus = CuriosCompat.getEquippedMaterialScalar(player, SockMaterial.BAMBOO, SockMaterial::jumpBoostBonus);
        if (jumpBonus > 0.0D) {
            int amplifier = Math.max(0, (int) Math.ceil(jumpBonus) - 1);
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 25, amplifier, false, false, true));
        }

        double fleshScalar = CuriosCompat.getEquippedMaterialScalar(player, SockMaterial.FLESH, SockMaterial::fleshRegenBonus);
        if (fleshScalar > 0.0D && player.tickCount % 40 == 0 && player.getFoodData().getFoodLevel() < 18) {
            player.heal((float) fleshScalar);
        }

        double silkScalar = CuriosCompat.getEquippedMaterialScalar(player, SockMaterial.SILK, SockMaterial::wallClimbBonus);
        if (silkScalar > 0.0D && player.horizontalCollision && !player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().x, Math.max(player.getDeltaMovement().y, 0.08D + silkScalar * 0.04D), player.getDeltaMovement().z);
            player.fallDistance = 0.0F;
        }

        if (ModList.get().isLoaded("immortalers_delight")) {
            tickSnifferFur(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (stack.is(ModItems.FUSHENG_ORIGINAL_SCENT.get())) {
            FushengOriginalScentItem.bindOwner(stack, event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getEntity().getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof OriginalScentItem originalScentItem)) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity livingEntity)) {
            return;
        }

        InteractionResult result = originalScentItem.handleEntityInteraction(stack, event.getEntity(), livingEntity, event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        if (entity instanceof ItemEntity itemEntity) {
            tickGroundFushengSock(itemEntity);
            return;
        }

        if (entity instanceof net.minecraft.world.entity.monster.piglin.Piglin piglin
                && ModList.get().isLoaded("immortalers_delight")) {
            ImmortalersDelightCompat.tickPiglinBarter(piglin);
        }

        if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.LUST) && livingEntity instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    private static void tickGroundFushengSock(ItemEntity itemEntity) {
        if (!itemEntity.onGround() || itemEntity.tickCount % 20 != 0) {
            return;
        }
        if (!itemEntity.getItem().is(ModItems.FUSHENG_ORIGINAL_SCENT.get())) {
            return;
        }
        if (!(itemEntity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        FushengOriginalScentItem.tickDroppedItem(serverLevel, itemEntity);
    }

    private static void tickSnifferFur(ServerPlayer player) {
        java.util.List<ItemStack> equippedSocks = CuriosCompat.getEquippedWearableSocks(player);
        boolean hasSnifferFur = equippedSocks.stream().anyMatch(stack ->
                SockDataUtil.getMaterialScalar(stack, SockMaterial.SNIFFER_FUR, material -> 1.0D) > 0.0D);
        if (!hasSnifferFur) {
            return;
        }

        double traveledDistance = getSnifferFurTravelDistance(player);
        if (!player.onGround()) {
            return;
        }

        double legacyProgress = player.getPersistentData().getDouble(LEGACY_TAG_SNIFFER_FUR_DISTANCE);
        for (ItemStack stack : equippedSocks) {
            double snifferFurShare = SockDataUtil.getMaterialScalar(stack, SockMaterial.SNIFFER_FUR, material -> 1.0D);
            if (snifferFurShare <= 0.0D) {
                continue;
            }

            if (legacyProgress > 0.0D && !SockDataUtil.hasSnifferFurDistance(stack)) {
                SockDataUtil.setSnifferFurDistance(stack, legacyProgress);
                player.getPersistentData().remove(LEGACY_TAG_SNIFFER_FUR_DISTANCE);
                legacyProgress = 0.0D;
            }

            double progress = SockDataUtil.getSnifferFurDistance(stack)
                    + traveledDistance * snifferFurShare;
            if (progress < SockDataUtil.SNIFFER_FUR_DISTANCE_PER_SEED) {
                SockDataUtil.setSnifferFurDistance(stack, progress);
                continue;
            }

            SockDataUtil.setSnifferFurDistance(stack, progress - SockDataUtil.SNIFFER_FUR_DISTANCE_PER_SEED);
            ImmortalersDelightCompat.dropRandomSnifferSeed(player);
            player.playNotifySound(SoundEvents.SNIFFER_DROP_SEED, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.precipitate_power.sniffer_fur_seed_found"), false);
            ModAdvancements.grant(player, ModAdvancements.SNIFFER_SOCK);
        }
    }

    private static double getSnifferFurTravelDistance(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(TAG_SNIFFER_FUR_LAST_X) || !data.contains(TAG_SNIFFER_FUR_LAST_Z)) {
            data.putDouble(TAG_SNIFFER_FUR_LAST_X, player.getX());
            data.putDouble(TAG_SNIFFER_FUR_LAST_Z, player.getZ());
            return 0.0D;
        }

        double deltaX = player.getX() - data.getDouble(TAG_SNIFFER_FUR_LAST_X);
        double deltaZ = player.getZ() - data.getDouble(TAG_SNIFFER_FUR_LAST_Z);
        data.putDouble(TAG_SNIFFER_FUR_LAST_X, player.getX());
        data.putDouble(TAG_SNIFFER_FUR_LAST_Z, player.getZ());
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return distance <= MAX_SNIFFER_FUR_DISTANCE_PER_TICK ? distance : 0.0D;
    }

    private static boolean hasFiveMaterialSock(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (SockDataUtil.getMaterials(stack).size() >= 5) {
                return true;
            }
        }
        return CuriosCompat.hasEquippedMatching(player, stack -> SockDataUtil.getMaterials(stack).size() >= 5);
    }
}
