package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class BoatSockItem extends WhiteSockItem {
    public BoatSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        SockDataUtil.initializeBoatSock(stack, 1);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.translatable(
                    "tooltip.precipitate_power.boat_sock.detail",
                    SockDataUtil.getBoatSockCapacityBoost(stack)
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.boat_sock.summary").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.travel_disposable_sock.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
