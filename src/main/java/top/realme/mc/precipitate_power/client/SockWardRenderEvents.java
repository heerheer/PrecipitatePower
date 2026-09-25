package top.realme.mc.precipitate_power.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModEffects;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.spell.SockWardSpell;

@EventBusSubscriber(modid = PrecipitatePower.MODID, value = Dist.CLIENT)
public final class SockWardRenderEvents {
    private SockWardRenderEvents() {
    }

    @SubscribeEvent
    public static void renderSockWard(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance ward = entity.getEffect(ModEffects.SOCK_WARD);
        if (ward == null) {
            return;
        }

        renderWard(entity, ward, event.getPartialTick(), event.getPoseStack(),
                event.getMultiBufferSource(), event.getPackedLight(), false);
    }

    @SubscribeEvent
    public static void renderFirstPersonSockWard(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        MobEffectInstance ward = player.getEffect(ModEffects.SOCK_WARD);
        if (ward == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cameraPosition = event.getCamera().getPosition();
        double playerX = Mth.lerp(partialTick, player.xOld, player.getX());
        double playerY = Mth.lerp(partialTick, player.yOld, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zOld, player.getZ());
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(
                playerX - cameraPosition.x,
                playerY - cameraPosition.y,
                playerZ - cameraPosition.z);
        renderWard(player, ward, partialTick, poseStack, buffer,
                LevelRenderer.getLightColor(player.level(), player.blockPosition()), true);
        poseStack.popPose();
        buffer.endBatch();
    }

    private static void renderWard(LivingEntity entity, MobEffectInstance ward, float partialTick,
                                   PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                   boolean firstPerson) {

        int sockCount = SockWardSpell.getRemainingSockCount(ward.getAmplifier());
        float age = entity.tickCount + partialTick;
        float radius = Math.max(0.8F, entity.getBbWidth() * 0.65F + 0.55F);
        float centerHeight = firstPerson ? entity.getEyeHeight() * 0.78F : entity.getBbHeight() * 0.55F;
        float scale = firstPerson ? 0.48F : 0.62F;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        for (int i = 0; i < sockCount; i++) {
            float angle = age * 0.09F + i * Mth.TWO_PI / sockCount;
            float verticalOffset = Mth.sin(angle * 2.0F) * 0.18F;
            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle) * radius,
                    centerHeight + verticalOffset,
                    Mth.sin(angle) * radius);
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.mulPose(Axis.ZP.rotation(angle + age * 0.12F));
            poseStack.scale(scale, scale, scale);
            itemRenderer.renderStatic(ModItems.WHITE_SOCK.get().getDefaultInstance(), ItemDisplayContext.GROUND,
                    packedLight, OverlayTexture.NO_OVERLAY, poseStack,
                    buffer, entity.level(), entity.getId() + i + 1);
            poseStack.popPose();
        }
    }
}
