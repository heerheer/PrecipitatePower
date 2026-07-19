package top.realme.mc.precipitate_power.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.item.ChesedOriginalScentItem;
import top.realme.mc.precipitate_power.registry.ModDataComponents;

public class ChesedFeedingItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer,
                          int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        LocalPlayer player = minecraft.player;
        boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean firstPerson = leftHand || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean feeding = player != null && player.isUsingItem()
                && player.getUseItem().getItem() == stack.getItem()
                && stack.has(ModDataComponents.CHESED_FEEDING_CHEESE.get());
        ItemStack cheese = ChesedOriginalScentItem.getFeedingCheese(stack);

        poseStack.pushPose();
        if (feeding && !cheese.isEmpty()) {
            poseStack.pushPose();
            int modifier = leftHand ? -1 : 1;
            poseStack.mulPose(Axis.YP.rotationDegrees(modifier * 40.0F));

            float partialTicks = AnimationTickHolder.getPartialTicks();
            float time = player.getUseItemRemainingTicks() - partialTicks + 1.0F;
            if (time / stack.getUseDuration(player) < 0.8F) {
                float rubbing = -Mth.abs(Mth.cos(time / 4.0F * Mth.PI) * 0.1F);
                poseStack.translate(0.0F, rubbing, 0.0F);
            }
            itemRenderer.renderStatic(cheese.copyWithCount(1), ItemDisplayContext.GUI, light, overlay,
                    poseStack, buffer, player.level(), 0);
            poseStack.popPose();
        }

        if (feeding && firstPerson) {
            int modifier = leftHand ? -1 : 1;
            poseStack.translate(modifier * 0.5F, 0.0F, -0.25F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(modifier * 40.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(modifier * 10.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(modifier * 90.0F));
        }

        itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, light, overlay,
                model.getOriginalModel());
        poseStack.popPose();
    }
}
