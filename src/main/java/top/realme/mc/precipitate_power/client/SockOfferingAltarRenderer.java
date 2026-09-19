package top.realme.mc.precipitate_power.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import top.realme.mc.precipitate_power.entity.SockOfferingAltarEntity;
import top.realme.mc.precipitate_power.registry.ModItems;

public class SockOfferingAltarRenderer extends EntityRenderer<SockOfferingAltarEntity> {
    private final ItemRenderer itemRenderer;
    private final ItemStack altar = new ItemStack(Blocks.ENCHANTING_TABLE);
    private final ItemStack sock = new ItemStack(ModItems.WHITE_SOCK.get());

    public SockOfferingAltarRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.0F;
    }

    @Override
    public void render(SockOfferingAltarEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int light = Math.max(packedLight, LightTexture.FULL_BRIGHT);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.scale(1.05F, 1.05F, 1.05F);
        itemRenderer.renderStatic(altar, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();

        float age = entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.45D + Math.sin(age * 0.09D) * 0.08D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 4.0F));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        itemRenderer.renderStatic(sock, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId() + 1);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SockOfferingAltarEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
