package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.registry.ModAdvancements;

public class OriginalScentItem extends AbstractSockItem {
    private final String tooltipKeyPrefix;
    private final String targetPlayerId;

    public OriginalScentItem(Properties properties, String tooltipKeyPrefix, String targetPlayerId) {
        super(properties);
        this.tooltipKeyPrefix = tooltipKeyPrefix;
        this.targetPlayerId = targetPlayerId;
    }

    protected String tooltipKeyPrefix() {
        return this.tooltipKeyPrefix;
    }

    @Override
    public boolean isWearableSock(ItemStack stack) {
        return false;
    }

    @Override
    public boolean rollMaterialsOnGeneration() {
        return false;
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        return GeneratorTickResult.handled(0, 0, false, context.inputStack(), ItemStack.EMPTY);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return handleEntityInteraction(stack, player, interactionTarget, usedHand);
    }

    public InteractionResult handleEntityInteraction(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!(interactionTarget instanceof Player targetPlayer) || !isTargetPlayer(targetPlayer)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        mergeIntoTarget(serverLevel, stack, player, targetPlayer);
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    protected boolean isTargetPlayer(Player targetPlayer) {
        return this.targetPlayerId.equalsIgnoreCase(targetPlayer.getGameProfile().getName())
                || this.targetPlayerId.equalsIgnoreCase(targetPlayer.getStringUUID());
    }

    protected void mergeIntoTarget(ServerLevel serverLevel, ItemStack stack, Player player, Player targetPlayer) {
        double centerX = targetPlayer.getX();
        double centerY = targetPlayer.getY() + targetPlayer.getBbHeight() * 0.5D;
        double centerZ = targetPlayer.getZ();
        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.copy()), centerX, centerY, centerZ, 32, 0.22D, 0.45D, 0.22D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, centerX, centerY, centerZ, 48, 0.35D, 0.65D, 0.35D, 0.45D);
        targetPlayer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.7F);
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.precipitate_power.original_scent_merged", stack.getHoverName(), targetPlayer.getDisplayName()),
                false
        );
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.SOCK_FINAL_HOME);
        }
    }

    protected void appendExtraShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(this.tooltipKeyPrefix + ".summary").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.translatable(this.tooltipKeyPrefix + ".detail").withStyle(ChatFormatting.GRAY));
        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.translatable(this.tooltipKeyPrefix + ".shift_detail").withStyle(ChatFormatting.AQUA));
            appendExtraShiftTooltip(stack, context, tooltipComponents, tooltipFlag);
        } else {
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.original_scent.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
