package top.realme.mc.precipitate_power.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import top.realme.mc.precipitate_power.registry.ModAdvancements;

public class DatouOriginalScentItem extends OriginalScentItem {
    private static final String TAG_ENTITY_USED = "DatouOriginalScentUsed";
    private static final String TAG_VILLAGER_COUNT = "DatouVillagerUpgradeCount";
    private static final String TAG_ENEMY_COUNT = "DatouEnemyDropCount";
    private static final String TAG_AWAKENED = "DatouOriginalScentAwakened";
    private static final int TARGET_COUNT = 100;
    private static final int PLAYER_DROP_COOLDOWN_TICKS = 20 * 600;
    private static final int MAX_LOOT_ATTEMPTS = 8;

    public DatouOriginalScentItem(Properties properties) {
        super(properties, "tooltip.precipitate_power.datou_original_scent", "MarverlousDT");
    }

    @Override
    protected void appendExtraShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(tooltipKeyPrefix() + ".villager_count", getVillagerUpgradeCount(stack), TARGET_COUNT).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable(tooltipKeyPrefix() + ".enemy_count", getEnemyDropCount(stack), TARGET_COUNT).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable(isAwakened(stack)
                ? tooltipKeyPrefix() + ".awakened"
                : tooltipKeyPrefix() + ".dormant").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return handleEntityInteraction(stack, player, interactionTarget, usedHand);
    }

    @Override
    public InteractionResult handleEntityInteraction(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        InteractionResult mergeResult = super.handleEntityInteraction(stack, player, interactionTarget, usedHand);
        if (mergeResult != InteractionResult.PASS) {
            return mergeResult;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (interactionTarget instanceof Villager villager) {
            return handleVillagerInteraction(serverLevel, stack, player, villager);
        }
        if (interactionTarget instanceof Player targetPlayer) {
            return handlePlayerInteraction(stack, player, targetPlayer);
        }
        if (interactionTarget instanceof Enemy) {
            return handleEnemyInteraction(serverLevel, stack, player, interactionTarget);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult handleVillagerInteraction(ServerLevel level, ItemStack stack, Player player, Villager villager) {
        if (hasBeenUsedOnEntity(villager)) {
            return InteractionResult.FAIL;
        }
        int beforeLevel = villager.getVillagerData().getLevel();
        if (beforeLevel >= 5) {
            return InteractionResult.FAIL;
        }

        villager.setVillagerData(villager.getVillagerData().setLevel(beforeLevel + 1));
        if (villager.getVillagerData().getLevel() <= beforeLevel) {
            return InteractionResult.FAIL;
        }

        markEntityUsed(villager);
        level.sendParticles(ParticleTypes.WITCH, villager.getX(), villager.getY() + 1.0D, villager.getZ(), 12, 0.35D, 0.45D, 0.35D, 0.02D);
        int newCount = incrementVillagerUpgradeCount(stack);
        awakenIfReady(stack, player, newCount, getEnemyDropCount(stack));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleEnemyInteraction(ServerLevel level, ItemStack stack, Player player, LivingEntity target) {
        if (hasBeenUsedOnEntity(target)) {
            return InteractionResult.FAIL;
        }

        ItemStack generatedDrop = createSingleEnemyDrop(level, player, target);
        if (generatedDrop.isEmpty()) {
            return InteractionResult.FAIL;
        }

        markEntityUsed(target);
        target.spawnAtLocation(generatedDrop.copy());
        int newCount = incrementEnemyDropCount(stack);
        awakenIfReady(stack, player, getVillagerUpgradeCount(stack), newCount);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePlayerInteraction(ItemStack stack, Player player, Player targetPlayer) {
        if (!isAwakened(stack)) {
            return InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        ItemStack dropped = dropRandomInventoryItem(targetPlayer);
        if (dropped.isEmpty()) {
            return InteractionResult.FAIL;
        }

        targetPlayer.displayClientMessage(Component.translatable("message.precipitate_power.datou_original_scent_player_drop"), true);
        player.getCooldowns().addCooldown(this, PLAYER_DROP_COOLDOWN_TICKS);
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.PLAYERS_CAN_GENERATE_POWER);
        }
        return InteractionResult.SUCCESS;
    }

    private static ItemStack dropRandomInventoryItem(Player targetPlayer) {
        Inventory inventory = targetPlayer.getInventory();
        List<Integer> nonEmptySlots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                nonEmptySlots.add(slot);
            }
        }
        if (nonEmptySlots.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int selectedSlot = nonEmptySlots.get(targetPlayer.getRandom().nextInt(nonEmptySlots.size()));
        ItemStack removed = inventory.removeItem(selectedSlot, 1);
        if (!removed.isEmpty()) {
            targetPlayer.spawnAtLocation(removed.copy());
            inventory.setChanged();
        }
        return removed;
    }

    private static ItemStack createSingleEnemyDrop(ServerLevel level, Player player, LivingEntity target) {
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(target.getLootTable());
        for (int i = 0; i < MAX_LOOT_ATTEMPTS; i++) {
            ItemStack generated = createSingleEnemyDropAttempt(level, player, target, lootTable);
            if (!generated.isEmpty()) {
                return generated;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createSingleEnemyDropAttempt(ServerLevel level, Player player, LivingEntity target, LootTable lootTable) {
        DamageSource damageSource = player.damageSources().playerAttack(player);
        LootParams lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .create(LootContextParamSets.ENTITY);
        ObjectArrayList<ItemStack> generated = lootTable.getRandomItems(lootParams);
        generated.removeIf(ItemStack::isEmpty);
        if (generated.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack selected = generated.get(level.random.nextInt(generated.size()));
        return selected.copy();
    }

    private static boolean hasBeenUsedOnEntity(Entity entity) {
        return entity.getPersistentData().getBoolean(TAG_ENTITY_USED);
    }

    private static void markEntityUsed(Entity entity) {
        entity.getPersistentData().putBoolean(TAG_ENTITY_USED, true);
    }

    private void awakenIfReady(ItemStack stack, Player player, int villagerCount, int enemyCount) {
        if (isAwakened(stack) || villagerCount < TARGET_COUNT || enemyCount < TARGET_COUNT) {
            return;
        }
        updateData(stack, tag -> tag.putBoolean(TAG_AWAKENED, true));
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.POWER_BELONGS_TO_DT);
        }
    }

    private static boolean isAwakened(ItemStack stack) {
        return getData(stack).getBoolean(TAG_AWAKENED);
    }

    private static int getVillagerUpgradeCount(ItemStack stack) {
        return Math.max(0, getData(stack).getInt(TAG_VILLAGER_COUNT));
    }

    private static int getEnemyDropCount(ItemStack stack) {
        return Math.max(0, getData(stack).getInt(TAG_ENEMY_COUNT));
    }

    private static int incrementVillagerUpgradeCount(ItemStack stack) {
        int updated = getVillagerUpgradeCount(stack) + 1;
        updateData(stack, tag -> tag.putInt(TAG_VILLAGER_COUNT, updated));
        return updated;
    }

    private static int incrementEnemyDropCount(ItemStack stack) {
        int updated = getEnemyDropCount(stack) + 1;
        updateData(stack, tag -> tag.putInt(TAG_ENEMY_COUNT, updated));
        return updated;
    }

    private static CompoundTag getData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void updateData(ItemStack stack, java.util.function.Consumer<CompoundTag> consumer) {
        CustomData updated = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(consumer);
        stack.set(DataComponents.CUSTOM_DATA, updated);
    }
}
