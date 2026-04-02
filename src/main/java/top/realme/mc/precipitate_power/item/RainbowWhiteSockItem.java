package top.realme.mc.precipitate_power.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import top.realme.mc.precipitate_power.util.SockDataUtil;

import java.util.List;

public class RainbowWhiteSockItem extends WhiteSockItem {
    public RainbowWhiteSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        SockDataUtil.initializeRainbowSock(stack);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag){
        super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
        tooltipComponents.add(
                Component.literal( "比某二字游戏抽出五星角色概率高").withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
