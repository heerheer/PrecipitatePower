package top.realme.mc.precipitate_power.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
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
import top.realme.mc.precipitate_power.item.ChesedOriginalScentItem;
import top.realme.mc.precipitate_power.item.ColorfulBurningBananaItem;
import top.realme.mc.precipitate_power.item.ChesedSockData;
import top.realme.mc.precipitate_power.item.OriginalScentItem;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.registry.ModAdvancements;
import top.realme.mc.precipitate_power.registry.ModEffects;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.registry.ModBlocks;
import top.realme.mc.precipitate_power.util.SockDataUtil;

@EventBusSubscriber(modid = PrecipitatePower.MODID)
public final class ModEvents {
    private static final ThreadLocal<Boolean> APPLYING_CHESED_ADDITIONAL_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final double CHESED_STATE_SWITCH_CHANCE = 0.20D;
    private static final double CHESED_CHEESE_GENERATION_CHANCE = 0.20D;
    private static final ResourceLocation CHESED_ABSORPTION_CAPACITY_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "chesed_absorption_capacity");
    private static final String LEGACY_TAG_SNIFFER_FUR_DISTANCE = "PrecipitatePowerSnifferFurDistance";
    private static final String TAG_SNIFFER_FUR_LAST_X = "PrecipitatePowerSnifferFurLastX";
    private static final String TAG_SNIFFER_FUR_LAST_Z = "PrecipitatePowerSnifferFurLastZ";
    private static final double MAX_SNIFFER_FUR_DISTANCE_PER_TICK = 2.0D;
    private static final String TAG_CHEESE_MILKING_FIRST_TIME = "FreshPressedCheeseFirstMilking";
    private static final String TAG_CHEESE_MILKING_COUNT = "FreshPressedCheeseMilkingCount";
    private static final long CHEESE_MILKING_WINDOW_TICKS = 20L * 60L * 5L;
    private static final String TAG_FRAGILE_STOMACH_WAS_SHIFTING = "FragileStomachWasShifting";
    private static final String TAG_FRAGILE_STOMACH_SHIFT_COUNT = "FragileStomachShiftCount";
    private static final String TAG_FRAGILE_STOMACH_LAST_PRESS = "FragileStomachLastPress";
    private static final int FRAGILE_STOMACH_REQUIRED_PRESSES = 3;
    private static final long FRAGILE_STOMACH_PRESS_WINDOW_TICKS = 20L * 3L;

    private ModEvents() {
    }

    @SubscribeEvent
    public static void onChesedCheeseUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack used = player.getItemInHand(event.getHand());
        InteractionHand sockHand = event.getHand() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack sock = player.getItemInHand(sockHand);
        if (!used.is(ChesedOriginalScentItem.CHEESE_TAG)
                || !(sock.getItem() instanceof ChesedOriginalScentItem chesed)
                || player.getCooldowns().isOnCooldown(chesed)
                || !ChesedOriginalScentItem.getData(sock).canFeed()) {
            return;
        }
        if (!chesed.tryStartFeeding(player, sockHand)) {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onChesedIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target instanceof Player player) {
            ItemStack held = findChesed(player, false);
            if (!held.isEmpty() && ChesedOriginalScentItem.getData(held).state() == ChesedSockData.State.SIDE) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, true, true));
            }
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)
                || event.getSource().getDirectEntity() != attacker) {
            return;
        }
        ItemStack held = findChesed(attacker, true);
        if (held.isEmpty()) {
            return;
        }
        ChesedSockData data = ChesedOriginalScentItem.getData(held);
        if (data.damagePercent() > 0) {
            event.setAmount(event.getAmount() * (1.0F + data.damagePercent() / 100.0F));
        }
    }

    @SubscribeEvent
    public static void onChesedDamagePost(LivingDamageEvent.Post event) {
        if (!APPLYING_CHESED_ADDITIONAL_DAMAGE.get()
                && event.getNewDamage() > 0.0F
                && event.getSource().getEntity() != null
                && event.getEntity() instanceof Player damagedPlayer
                && !damagedPlayer.level().isClientSide()) {
            ItemStack damagedPlayerSock = findChesed(damagedPlayer, false);
            if (!damagedPlayerSock.isEmpty()
                    && ChesedOriginalScentItem.getData(damagedPlayerSock).state()
                    == ChesedSockData.State.HALF_ONE_DO_ZERO
                    && damagedPlayer.getRandom().nextDouble() < CHESED_CHEESE_GENERATION_CHANCE) {
                grantChesedCheese(damagedPlayer);
            }
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)
                || event.getSource().getDirectEntity() != attacker) {
            return;
        }
        ItemStack held = findChesed(attacker, true);
        if (held.isEmpty()) {
            return;
        }
        ChesedSockData data = ChesedOriginalScentItem.getData(held);
        if (data.state() == ChesedSockData.State.GATE) {
            float absorptionBefore = attacker.getAbsorptionAmount();
            float absorptionGained = event.getNewDamage() * 0.05F;
            float targetAbsorption = absorptionBefore + absorptionGained;
            ensureChesedAbsorptionCapacity(attacker, targetAbsorption);
            attacker.setAbsorptionAmount(targetAbsorption);
        }
        if (APPLYING_CHESED_ADDITIONAL_DAMAGE.get()) {
            return;
        }

        if (data.state() == ChesedSockData.State.HALF_ZERO_DO_ONE
                && !attacker.level().isClientSide()
                && attacker.getRandom().nextDouble() < CHESED_CHEESE_GENERATION_CHANCE) {
            grantChesedCheese(attacker);
        }

        LivingEntity target = event.getEntity();
        int extraHits = data.additionalHits();
        if (data.state() == ChesedSockData.State.BEAR_HOTEL
                && target.getHealth() + event.getNewDamage() > attacker.getHealth()) {
            extraHits++;
        }
        if (data.additionalDamage() > 0) {
            for (int i = 0; i < extraHits && target.isAlive(); i++) {
                try {
                    APPLYING_CHESED_ADDITIONAL_DAMAGE.set(true);
                    target.invulnerableTime = 0;
                    DamageSource source = attacker.damageSources().playerAttack(attacker);
                    if (target.hurt(source, data.additionalDamage()) && target.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        EnchantmentHelper.doPostAttackEffects(serverLevel, target, source);
                    }
                } finally {
                    APPLYING_CHESED_ADDITIONAL_DAMAGE.set(false);
                }
            }
        }

        if (attacker.getRandom().nextDouble() < CHESED_STATE_SWITCH_CHANCE) {
            ChesedOriginalScentItem.setData(held, data.switchState(attacker.getRandom()));
        }
    }

    private static void ensureChesedAbsorptionCapacity(Player player, float targetAbsorption) {
        AttributeInstance maxAbsorption = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorption == null || maxAbsorption.getValue() >= targetAbsorption) {
            return;
        }

        AttributeModifier existing = maxAbsorption.getModifier(CHESED_ABSORPTION_CAPACITY_ID);
        double addedCapacity = targetAbsorption - maxAbsorption.getValue();
        double modifierAmount = addedCapacity + (existing == null ? 0.0D : existing.amount());
        maxAbsorption.addOrUpdateTransientModifier(new AttributeModifier(
                CHESED_ABSORPTION_CAPACITY_ID, modifierAmount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void grantChesedCheese(Player player) {
        ItemStack cheese = new ItemStack(ModItems.TEST_CHEESE.get());
        if (!player.getInventory().add(cheese)) {
            player.drop(cheese, false);
        }
    }

    private static ItemStack findChesed(Player player, boolean mainHandOnly) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ChesedOriginalScentItem) {
            return main;
        }
        if (!mainHandOnly && player.getOffhandItem().getItem() instanceof ChesedOriginalScentItem) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
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

        if (player.tickCount % 20 == 0) {
            ColorfulBurningBananaItem.applyHealthBonus(player);
        }

        tickFragileStomach(serverPlayer);

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

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (tryCollectFreshPressedCheese(event)) {
            return;
        }
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

    private static void tickFragileStomach(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        boolean shifting = player.isShiftKeyDown();
        boolean wasShifting = data.getBoolean(TAG_FRAGILE_STOMACH_WAS_SHIFTING);
        data.putBoolean(TAG_FRAGILE_STOMACH_WAS_SHIFTING, shifting);

        if (!player.hasEffect(ModEffects.FRAGILE_STOMACH)) {
            data.remove(TAG_FRAGILE_STOMACH_SHIFT_COUNT);
            data.remove(TAG_FRAGILE_STOMACH_LAST_PRESS);
            return;
        }
        if (!shifting || wasShifting) {
            return;
        }

        long now = player.serverLevel().getGameTime();
        long lastPress = data.getLong(TAG_FRAGILE_STOMACH_LAST_PRESS);
        int presses = now - lastPress <= FRAGILE_STOMACH_PRESS_WINDOW_TICKS
                ? data.getInt(TAG_FRAGILE_STOMACH_SHIFT_COUNT) + 1
                : 1;
        data.putLong(TAG_FRAGILE_STOMACH_LAST_PRESS, now);
        data.putInt(TAG_FRAGILE_STOMACH_SHIFT_COUNT, presses);
        if (presses < FRAGILE_STOMACH_REQUIRED_PRESSES) {
            return;
        }

        BlockPos poopPos = player.blockPosition();
        var poopState = ModBlocks.BANANA_POOP.get().defaultBlockState();
        if (!player.serverLevel().getBlockState(poopPos).canBeReplaced()
                || !poopState.canSurvive(player.serverLevel(), poopPos)) {
            return;
        }

        if (!player.serverLevel().setBlock(poopPos, poopState, 3)) {
            return;
        }
        ModAdvancements.grant(player, ModAdvancements.DO_NOT_POOP_ANYWHERE);
        player.removeEffect(ModEffects.FRAGILE_STOMACH);
        data.remove(TAG_FRAGILE_STOMACH_SHIFT_COUNT);
        data.remove(TAG_FRAGILE_STOMACH_LAST_PRESS);
        player.serverLevel().playSound(null, poopPos, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.7F, 0.8F);
    }

    private static boolean tryCollectFreshPressedCheese(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Cow cow)
                || !cow.hasCustomName()
                || !"chesed".equalsIgnoreCase(cow.getCustomName().getString())
                || !event.getEntity().getItemInHand(event.getHand()).is(Items.BUCKET)) {
            return false;
        }

        if (cow.level().isClientSide()) {
            event.setCancellationResult(InteractionResult.sidedSuccess(true));
            event.setCanceled(true);
            return true;
        }

        long gameTime = cow.level().getGameTime();
        CompoundTag data = cow.getPersistentData();
        long firstTime = data.getLong(TAG_CHEESE_MILKING_FIRST_TIME);
        int count = data.getInt(TAG_CHEESE_MILKING_COUNT);
        boolean inWindow = count > 0 && gameTime - firstTime < CHEESE_MILKING_WINDOW_TICKS;
        if (!inWindow) {
            firstTime = gameTime;
            count = 0;
        }

        count++;
        data.putLong(TAG_CHEESE_MILKING_FIRST_TIME, firstTime);
        data.putInt(TAG_CHEESE_MILKING_COUNT, count);
        ItemStack result = new ItemStack(count <= 3
                ? ModItems.CONCENTRATED_FRESH_PRESSED_CHEESE_BUCKET.get()
                : ModItems.DILUTED_FRESH_PRESSED_CHEESE_BUCKET.get());
        if (count > 3) {
            cow.hurt(cow.damageSources().playerAttack(event.getEntity()), 1.0F);
        }

        event.getEntity().setItemInHand(event.getHand(), ItemUtils.createFilledResult(
                event.getEntity().getItemInHand(event.getHand()), event.getEntity(), result));
        cow.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        return true;
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
