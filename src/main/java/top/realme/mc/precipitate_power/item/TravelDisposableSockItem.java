package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.Config;

public class TravelDisposableSockItem extends WhiteSockItem {
    public TravelDisposableSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.translatable(
                    "tooltip.precipitate_power.travel_disposable_sock.detail",
                    Config.TRAVEL_SOCK_GENERATION_MULTIPLIER.get(),
                    Config.TRAVEL_SOCK_MAX_EXTRACT_BOOST.get()
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.travel_disposable_sock.summary").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.travel_disposable_sock.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
