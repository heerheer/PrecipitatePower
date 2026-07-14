package top.realme.mc.precipitate_power.event;

import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.compat.curios.CuriosCompat;
import top.realme.mc.precipitate_power.item.FushengOriginalScentItem;
import top.realme.mc.precipitate_power.item.OriginalScentItem;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.registry.ModAdvancements;
import top.realme.mc.precipitate_power.registry.ModEffects;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.SockDataUtil;

@EventBusSubscriber(modid = PrecipitatePower.MODID)
public final class ModEvents {
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

    private static boolean hasFiveMaterialSock(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (SockDataUtil.getMaterials(stack).size() >= 5) {
                return true;
            }
        }
        return CuriosCompat.hasEquippedMatching(player, stack -> SockDataUtil.getMaterials(stack).size() >= 5);
    }
}
