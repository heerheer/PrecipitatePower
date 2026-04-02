package top.realme.mc.precipitate_power.item;

import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class RainbowWhiteSockItem extends WhiteSockItem {
    public RainbowWhiteSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        SockDataUtil.initializeRainbowSock(stack);
        return stack;
    }
}
