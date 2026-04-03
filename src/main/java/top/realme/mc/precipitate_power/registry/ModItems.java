package top.realme.mc.precipitate_power.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.item.DirtyWhiteSockItem;
import top.realme.mc.precipitate_power.item.RainbowWhiteSockItem;
import top.realme.mc.precipitate_power.item.WhiteSockItem;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(PrecipitatePower.MODID);

    public static final DeferredItem<Item> WHITE_SOCK = REGISTER.register(
            "white_sock",
            () -> new WhiteSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> DIRTY_WHITE_SOCK = REGISTER.register(
            "dirty_white_sock",
            () -> new DirtyWhiteSockItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> RAINBOW_WHITE_SOCK = REGISTER.register(
            "rainbow_white_sock",
            () -> new RainbowWhiteSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    private ModItems() {
    }
}
