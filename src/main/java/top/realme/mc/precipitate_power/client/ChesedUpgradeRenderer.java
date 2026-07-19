package top.realme.mc.precipitate_power.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import top.realme.mc.precipitate_power.entity.ChesedUpgradeEntity;

public class ChesedUpgradeRenderer extends EntityRenderer<ChesedUpgradeEntity> {
    private final ItemRenderer itemRenderer;

    public ChesedUpgradeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ChesedUpgradeEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float age = entity.tickCount + partialTick;
        float progress = Mth.clamp(age / ChesedUpgradeEntity.DURATION_TICKS, 0.0F, 1.0F);
        int animationLight = LightTexture.FULL_BRIGHT;
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotation(age * 0.06F));
        poseStack.scale(0.9F + 0.12F * Mth.sin(age * 0.15F), 0.9F + 0.12F * Mth.sin(age * 0.15F), 0.9F + 0.12F * Mth.sin(age * 0.15F));
        itemRenderer.renderStatic(entity.getSock(), ItemDisplayContext.GROUND, animationLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();

        int cheeseCount = entity.getCheeseCount();
        float radius = Mth.lerp(progress, 0.85F, 0.08F);
        float scale = Mth.lerp(progress, 0.5F, 0.08F);
        for (int i = 0; i < cheeseCount; i++) {
            float angle = age * 0.18F + i * Mth.TWO_PI / cheeseCount;
            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.translate(Mth.cos(angle) * radius, Mth.sin(angle) * radius * 0.75F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotation(angle + age * 0.04F));
            poseStack.scale(scale, scale, scale);
            itemRenderer.renderStatic(entity.getCheese(), ItemDisplayContext.GROUND, animationLight, OverlayTexture.NO_OVERLAY,
                    poseStack, buffer, entity.level(), entity.getId() + i + 1);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ChesedUpgradeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
