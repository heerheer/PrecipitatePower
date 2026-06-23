package top.realme.mc.precipitate_power.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.item.BasicStyledSockItem;
import top.realme.mc.precipitate_power.item.BoatSockItem;
import top.realme.mc.precipitate_power.item.DatouOriginalScentItem;
import top.realme.mc.precipitate_power.item.FushengOriginalScentItem;
import top.realme.mc.precipitate_power.item.OriginalScentItem;
import top.realme.mc.precipitate_power.item.RainbowWhiteSockItem;
import top.realme.mc.precipitate_power.item.TravelDisposableSockItem;
import top.realme.mc.precipitate_power.item.WhiteSockItem;
import top.realme.mc.precipitate_power.item.ZhaozhaoOriginalScentItem;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(PrecipitatePower.MODID);

    public static final DeferredItem<Item> WHITE_SOCK = REGISTER.register(
            "white_sock",
            () -> new WhiteSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> DIRTY_WHITE_SOCK = REGISTER.register(
            "dirty_white_sock",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> RAINBOW_WHITE_SOCK = REGISTER.register(
            "rainbow_white_sock",
            () -> new RainbowWhiteSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> TRAVEL_DISPOSABLE_SOCK = REGISTER.register(
            "travel_disposable_sock",
            () -> new TravelDisposableSockItem(new Item.Properties().stacksTo(1).durability(16))
    );

    public static final DeferredItem<Item> BOAT_SOCK = REGISTER.register(
            "boat_sock",
            () -> new BoatSockItem(new Item.Properties().stacksTo(1).durability(32))
    );

    public static final DeferredItem<Item> OVER_KNEE_SOCK = REGISTER.register(
            "over_knee_sock",
            () -> new BasicStyledSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> SPORT_CREW_SOCK = REGISTER.register(
            "sport_crew_sock",
            () -> new BasicStyledSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> PANTYHOSE = REGISTER.register(
            "pantyhose",
            () -> new BasicStyledSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> SPLIT_TOE_SOCK = REGISTER.register(
            "split_toe_sock",
            () -> new BasicStyledSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> STOCKINGS = REGISTER.register(
            "stockings",
            () -> new BasicStyledSockItem(new Item.Properties().stacksTo(1).durability(64))
    );

    public static final DeferredItem<Item> ZHAOZHAO_ORIGINAL_SCENT = REGISTER.register(
            "zhaozhao_original_scent",
            () -> new ZhaozhaoOriginalScentItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> FUSHENG_ORIGINAL_SCENT = REGISTER.register(
            "fusheng_original_scent",
            () -> new FushengOriginalScentItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> DATOU_ORIGINAL_SCENT = REGISTER.register(
            "datou_original_scent",
            () -> new DatouOriginalScentItem(new Item.Properties().stacksTo(1))
    );

    private ModItems() {
    }
}
