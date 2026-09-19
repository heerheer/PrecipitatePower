package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModAdvancements;

public class OriginalScentItem extends AbstractSockItem {
    private static final ResourceLocation FUSION_MAX_HEALTH_ID = ResourceLocation.fromNamespaceAndPath(
            PrecipitatePower.MODID, "original_scent_fusion_max_health");
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
    public boolean canPrecipitateInGenerator(ItemStack stack) {
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
        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.copy()),
                centerX, centerY, centerZ, 80, 0.35D, 0.65D, 0.35D, 0.09D);
        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                centerX, centerY, centerZ, 120, 0.7D, 1.0D, 0.7D, 0.7D);
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                centerX, centerY, centerZ, 64, 0.45D, 0.8D, 0.45D, 0.12D);
        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                centerX, centerY, centerZ, 48, 0.55D, 0.9D, 0.55D, 0.25D);
        serverLevel.sendParticles(ParticleTypes.FLASH,
                centerX, centerY, centerZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        spawnFusionParticleCloud(serverLevel, centerX, centerY, centerZ, ParticleTypes.ENCHANT, 1.8F);
        spawnFusionParticleCloud(serverLevel, centerX, centerY, centerZ, ParticleTypes.END_ROD, 1.2F);
        targetPlayer.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.7F);
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.precipitate_power.original_scent_merged", stack.getHoverName(), targetPlayer.getDisplayName()),
                false
        );
        grantFusionMaxHealth(player);
        grantFusionMaxHealth(targetPlayer);
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.SOCK_FINAL_HOME);
        }
    }

    private static void grantFusionMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        AttributeModifier existing = maxHealth.getModifier(FUSION_MAX_HEALTH_ID);
        double updatedBonus = (existing == null ? 0.0D : existing.amount()) + 1.0D;
        maxHealth.addOrReplacePermanentModifier(new AttributeModifier(
                FUSION_MAX_HEALTH_ID, updatedBonus, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void spawnFusionParticleCloud(
            ServerLevel level, double x, double y, double z,
            ParticleOptions particle, float radius) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, x, y, z);
        cloud.setParticle(particle);
        cloud.setRadius(radius);
        cloud.setRadiusPerTick(-(radius - 0.5F) / 100.0F);
        cloud.setWaitTime(0);
        cloud.setDuration(100);
        level.addFreshEntity(cloud);
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
