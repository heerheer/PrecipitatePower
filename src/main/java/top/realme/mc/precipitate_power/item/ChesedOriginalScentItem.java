package top.realme.mc.precipitate_power.item;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.CustomUseEffectsItem;
import com.simibubi.create.foundation.mixin.accessor.LivingEntityAccessor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.createmod.catnip.data.TriState;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.ChesedUpgradeEntity;
import top.realme.mc.precipitate_power.registry.ModAdvancements;
import top.realme.mc.precipitate_power.registry.ModDataComponents;

public class ChesedOriginalScentItem extends Item implements CustomUseEffectsItem {
    public static final TagKey<Item> CHEESE_TAG = TagKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "cheese")
    );
    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            PrecipitatePower.MODID, "chesed_damage");
    private static final ResourceLocation RANGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            PrecipitatePower.MODID, "chesed_attack_range");
    private static final int FEED_DURATION_TICKS = 32;
    private static final int FEED_COOLDOWN_TICKS = 20;

    public ChesedOriginalScentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack sock = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this) || !getData(sock).canFeed()) {
            return InteractionResultHolder.fail(sock);
        }
        return tryStartFeeding(player, hand)
                ? InteractionResultHolder.sidedSuccess(sock, level.isClientSide())
                : InteractionResultHolder.pass(sock);
    }

    public boolean tryStartFeeding(Player player, InteractionHand sockHand) {
        ItemStack sock = player.getItemInHand(sockHand);
        ItemStack storedCheese = getFeedingCheese(sock);
        if (!storedCheese.isEmpty()) {
            sock.remove(ModDataComponents.CHESED_FEEDING_CHEESE.get());
            restoreCheese(player, opposite(sockHand), storedCheese);
        }
        ItemStack cheese = getOtherHandStack(player, sockHand);
        if (player.getCooldowns().isOnCooldown(this) || !getData(sock).canFeed() || !cheese.is(CHEESE_TAG)) {
            return false;
        }
        sock.set(ModDataComponents.CHESED_FEEDING_CHEESE.get(),
                ItemContainerContents.fromItems(List.of(cheese.copy())));
        player.setItemInHand(opposite(sockHand), ItemStack.EMPTY);
        player.startUsingItem(sockHand);
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FEED_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (livingEntity instanceof Player player
                && getFeedingCheese(stack).isEmpty()) {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        ItemStack cheese = getFeedingCheese(stack);
        if (!cheese.isEmpty()) {
            stack.remove(ModDataComponents.CHESED_FEEDING_CHEESE.get());
            restoreCheese(player, opposite(player.getUsedItemHand()), cheese);
        }
    }

    @Override
    public TriState shouldTriggerUseEffects(ItemStack stack, LivingEntity entity) {
        return TriState.TRUE;
    }

    @Override
    public boolean triggerUseEffects(ItemStack stack, LivingEntity entity, int count, RandomSource random) {
        ItemStack cheese = getFeedingCheese(stack);
        if (!cheese.isEmpty()) {
            ((LivingEntityAccessor) entity).create$callSpawnItemParticles(cheese, 1);
        }
        if ((entity.getTicksUsingItem() - 6) % 7 == 0) {
            entity.playSound(AllSoundEvents.SANDING_SHORT.getMainEvent(),
                    0.9F + 0.2F * random.nextFloat(), random.nextFloat() * 0.2F + 0.9F);
        }
        return true;
    }

    @Override
    public SoundEvent getEatingSound() {
        return AllSoundEvents.SANDING_SHORT.getMainEvent();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack sock, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return sock;
        }

        InteractionHand cheeseHand = opposite(player.getUsedItemHand());
        ItemStack cheese = getFeedingCheese(sock);
        ChesedSockData current = getData(sock);
        if (cheese.isEmpty() || !cheese.is(CHEESE_TAG) || !current.canFeed()
                || player.getCooldowns().isOnCooldown(this)) {
            sock.remove(ModDataComponents.CHESED_FEEDING_CHEESE.get());
            if (!cheese.isEmpty()) {
                restoreCheese(player, cheeseHand, cheese);
            }
            return sock;
        }

        sock.remove(ModDataComponents.CHESED_FEEDING_CHEESE.get());
        int consumedCount = cheese.getCount();
        ItemStack consumedCheese = cheese.copyWithCount(1);
        if (player.getAbilities().instabuild) {
            restoreCheese(player, cheeseHand, cheese);
        } else if (cheese.hasCraftingRemainingItem()) {
            ItemStack remainders = cheese.getCraftingRemainingItem().copyWithCount(consumedCount);
            player.getInventory().placeItemBackInInventory(remainders);
        }

        ChesedSockData.FeedResult result = current.feed(serverLevel.random, consumedCount);
        setData(sock, result.data());
        player.getCooldowns().addCooldown(this, FEED_COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));

        if (!result.upgraded()) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getEyeY() - 0.25D, player.getZ(), 4,
                    0.2D, 0.15D, 0.2D, 0.01D);
            return sock;
        }

        player.playSound(SoundEvents.PLAYER_LEVELUP, 0.9F, 1.15F);
        boolean animationStarted = false;
        if (player instanceof ServerPlayer serverPlayer) {
            animationStarted = ChesedUpgradeEntity.spawn(serverLevel, serverPlayer, sock.copy(), consumedCheese);
        }
        if (result.data().level() == ChesedSockData.MAX_LEVEL && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.CHEESE_DE_DE_DE_ER);
        }
        return animationStarted ? ItemStack.EMPTY : sock;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ChesedSockData data = getData(stack);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        if (data.damage() > 0) {
            builder.add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(DAMAGE_MODIFIER_ID, data.damage(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (data.attackRange() > 0) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(RANGE_MODIFIER_ID, data.attackRange(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        return builder.build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        ChesedSockData data = getData(stack);
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.summary")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!flag.hasShiftDown()) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (data.level() >= ChesedSockData.MAX_LEVEL) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.level_max")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.level",
                    data.level(), data.cheeseProgress(), data.requiredCheese()).withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.damage", data.damage())
                .withStyle(ChatFormatting.RED));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.additional_damage", data.additionalDamage())
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.additional_hits", data.additionalHits())
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.damage_percent", data.damagePercent())
                .withStyle(ChatFormatting.RED));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.attack_range", data.attackRange())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.state",
                net.minecraft.network.chat.Component.translatable("tooltip.precipitate_power.chesed_original_scent.state."
                        + data.state().name().toLowerCase(java.util.Locale.ROOT))).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static ChesedSockData getData(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CHESED_SOCK_DATA.get(), ChesedSockData.DEFAULT);
    }

    public static void setData(ItemStack stack, ChesedSockData data) {
        stack.set(ModDataComponents.CHESED_SOCK_DATA.get(), data);
    }

    public static ItemStack getFeedingCheese(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CHESED_FEEDING_CHEESE.get(), ItemContainerContents.EMPTY)
                .nonEmptyStream()
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack getOtherHandStack(Player player, InteractionHand hand) {
        return player.getItemInHand(opposite(hand));
    }

    private static InteractionHand opposite(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static void restoreCheese(Player player, InteractionHand hand, ItemStack cheese) {
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, cheese);
        } else {
            player.getInventory().placeItemBackInInventory(cheese);
        }
    }
}
