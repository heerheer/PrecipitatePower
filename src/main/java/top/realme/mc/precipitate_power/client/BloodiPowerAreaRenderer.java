package top.realme.mc.precipitate_power.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.BloodiPowerAreaEntity;

public class BloodiPowerAreaRenderer extends EntityRenderer<BloodiPowerAreaEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            PrecipitatePower.MODID, "textures/gui/spell_icons/bloodi_power.png");

    public BloodiPowerAreaRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(BloodiPowerAreaEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float radius = entity.getRadius();
        float alpha = Math.min(0.82F, Math.max(0.25F, entity.getRemainingTicks() / 10.0F));
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.025D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 0.45F));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        vertex(vertices, matrix, -radius, -radius, 0.0F, 0.0F, alpha);
        vertex(vertices, matrix, -radius, radius, 0.0F, 1.0F, alpha);
        vertex(vertices, matrix, radius, radius, 1.0F, 1.0F, alpha);
        vertex(vertices, matrix, radius, -radius, 1.0F, 0.0F, alpha);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, float x, float z,
                               float u, float v, float alpha) {
        vertices.addVertex(matrix, x, 0.0F, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(BloodiPowerAreaEntity entity) {
        return TEXTURE;
    }
}
