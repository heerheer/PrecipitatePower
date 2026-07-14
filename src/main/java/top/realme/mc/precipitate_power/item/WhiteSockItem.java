package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class WhiteSockItem extends AbstractSockItem {
    public WhiteSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        SockDataUtil.appendTooltip(stack, tooltipComponents, tooltipFlag);
    }
}
