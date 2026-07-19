package top.realme.mc.precipitate_power.registry;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.item.BasicStyledSockItem;
import top.realme.mc.precipitate_power.item.BoatSockItem;
import top.realme.mc.precipitate_power.item.DatouOriginalScentItem;
import top.realme.mc.precipitate_power.item.ElectricSockItem;
import top.realme.mc.precipitate_power.item.ChesedOriginalScentItem;
import top.realme.mc.precipitate_power.item.FushengOriginalScentItem;
import top.realme.mc.precipitate_power.item.OriginalScentItem;
import top.realme.mc.precipitate_power.item.RainbowWhiteSockItem;
import top.realme.mc.precipitate_power.item.TravelDisposableSockItem;
import top.realme.mc.precipitate_power.item.WhiteSockItem;
import top.realme.mc.precipitate_power.item.ZhaozhaoOriginalScentItem;
import top.realme.mc.precipitate_power.Config;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

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

    public static final DeferredItem<Item> CHESED_ORIGINAL_SCENT = REGISTER.register(
            "chesed_original_scent",
            () -> new ChesedOriginalScentItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> TEST_CHEESE = REGISTER.register(
            "test_cheese",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> STIR_FRIED_SOCK = REGISTER.register(
            "stir_fried_sock",
            () -> new Item(new Item.Properties().food(new FoodProperties(
                    8,
                    0.8F,
                    true,
                    1.6F,
                    Optional.of(Items.BOWL.getDefaultInstance()),
                    List.of()
            )))
    );

    public static final DeferredItem<Item> SMALL_ELECTRIC_SOCK = REGISTER.register(
            "small_electric_sock",
            () -> new ElectricSockItem(new Item.Properties().stacksTo(1),
                    Config.SMALL_ELECTRIC_SOCK_CAPACITY::get,
                    Config.SMALL_ELECTRIC_SOCK_TRANSFER_RATE::get)
    );

    public static final DeferredItem<Item> MEDIUM_ELECTRIC_SOCK = REGISTER.register(
            "medium_electric_sock",
            () -> new ElectricSockItem(new Item.Properties().stacksTo(1),
                    Config.MEDIUM_ELECTRIC_SOCK_CAPACITY::get,
                    Config.MEDIUM_ELECTRIC_SOCK_TRANSFER_RATE::get)
    );

    public static final DeferredItem<Item> LARGE_ELECTRIC_SOCK = REGISTER.register(
            "large_electric_sock",
            () -> new ElectricSockItem(new Item.Properties().stacksTo(1),
                    Config.LARGE_ELECTRIC_SOCK_CAPACITY::get,
                    Config.LARGE_ELECTRIC_SOCK_TRANSFER_RATE::get)
    );

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> ((ElectricSockItem) stack.getItem()).createEnergyStorage(stack),
                SMALL_ELECTRIC_SOCK.get(), MEDIUM_ELECTRIC_SOCK.get(), LARGE_ELECTRIC_SOCK.get());
    }

    private ModItems() {
    }
}
