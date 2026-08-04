package top.realme.mc.precipitate_power;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import top.realme.mc.precipitate_power.compat.curios.CuriosCompat;
import top.realme.mc.precipitate_power.compat.kaleidoscopetavern.KaleidoscopeTavernCompat;
import top.realme.mc.precipitate_power.event.ModEvents;
import top.realme.mc.precipitate_power.registry.ModBlockEntities;
import top.realme.mc.precipitate_power.registry.ModBlocks;
import top.realme.mc.precipitate_power.registry.ModEffects;
import top.realme.mc.precipitate_power.registry.ModFluids;
import top.realme.mc.precipitate_power.registry.ModDataComponents;
import top.realme.mc.precipitate_power.registry.ModEntities;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.registry.ModLootModifiers;
import top.realme.mc.precipitate_power.registry.ModMenus;
import top.realme.mc.precipitate_power.registry.ModRecipes;

@Mod(PrecipitatePower.MODID)
public class PrecipitatePower {
    public static final String MODID = "precipitate_power";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.precipitate_power.main"))
                    .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
                    .icon(() -> ModItems.WHITE_SOCK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WHITE_SOCK.get());
                        output.accept(ModItems.RAINBOW_WHITE_SOCK.get().getDefaultInstance());
                        output.accept(ModItems.TRAVEL_DISPOSABLE_SOCK.get());
                        output.accept(ModItems.BOAT_SOCK.get().getDefaultInstance());
                        output.accept(ModItems.OVER_KNEE_SOCK.get());
                        output.accept(ModItems.SPORT_CREW_SOCK.get());
                        output.accept(ModItems.PANTYHOSE.get());
                        output.accept(ModItems.SPLIT_TOE_SOCK.get());
                        output.accept(ModItems.STOCKINGS.get());
                        output.accept(ModItems.ZHAOZHAO_ORIGINAL_SCENT.get());
                        output.accept(ModItems.FUSHENG_ORIGINAL_SCENT.get());
                        output.accept(ModItems.DATOU_ORIGINAL_SCENT.get());
                        output.accept(ModItems.CHESED_ORIGINAL_SCENT.get());
                        output.accept(ModItems.TEST_CHEESE.get());
                        output.accept(ModItems.FRESH_PRESSED_CHEESE.get());
                        output.accept(ModItems.CONCENTRATED_FRESH_PRESSED_CHEESE_BUCKET.get());
                        output.accept(ModItems.DILUTED_FRESH_PRESSED_CHEESE_BUCKET.get());
                        output.accept(ModItems.BURNING_BANANA.get());
                        output.accept(ModItems.COLORFUL_BURNING_BANANA.get());
                        output.accept(ModItems.COLORFUL_BANANA_SLICE.get());
                        output.accept(ModBlocks.BANANA_POOP_ITEM.get());
                        output.accept(ModItems.DIRTY_WHITE_SOCK.get());
                        output.accept(ModItems.STIR_FRIED_SOCK.get());
                        output.accept(ModItems.SMALL_ELECTRIC_SOCK.get());
                        output.accept(ModItems.MEDIUM_ELECTRIC_SOCK.get());
                        output.accept(ModItems.LARGE_ELECTRIC_SOCK.get());
                        if (ModList.get().isLoaded(KaleidoscopeTavernCompat.MOD_ID)) {
                            output.accept(KaleidoscopeTavernCompat.createMaxQualityStack());
                        }
                        output.accept(ModBlocks.PRECIPITATE_GENERATOR_ITEM.get());
                        output.accept(ModBlocks.SOCK_BLENDER_ITEM.get());
                        if (ModBlocks.REGISTER_ADVANCED_PRECIPITATE_GENERATOR) {
                            output.accept(ModBlocks.ADVANCED_PRECIPITATE_GENERATOR_ITEM.get());
                        }
                    })
                    .build()
    );

    public PrecipitatePower(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.REGISTER.register(modEventBus);
        ModEntities.REGISTER.register(modEventBus);
        ModBlocks.REGISTER.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.BLOCKS.register(modEventBus);
        ModItems.REGISTER.register(modEventBus);
        ModBlockEntities.REGISTER.register(modEventBus);
        ModMenus.REGISTER.register(modEventBus);
        ModLootModifiers.REGISTER.register(modEventBus);
        ModEffects.REGISTER.register(modEventBus);
        ModRecipes.REGISTER.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        if (ModList.get().isLoaded(KaleidoscopeTavernCompat.MOD_ID)) {
            KaleidoscopeTavernCompat.register(modEventBus);
        }

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModBlocks::registerCapabilities); // 只能在这边注册cap
        modEventBus.addListener(ModItems::registerCapabilities);

        if (ModList.get().isLoaded("curios")) {
            modEventBus.addListener(CuriosCompat::onCommonSetup);
        }

        NeoForge.EVENT_BUS.register(ModEvents.class);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Precipitate Power common setup complete");
        if (!ModList.get().isLoaded("curios")) {
            LOGGER.info("Curios not found, skipping sock curio compat");
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.PRECIPITATE_GENERATOR_ITEM.get());
            event.accept(ModBlocks.SOCK_BLENDER_ITEM.get());
            if (ModBlocks.REGISTER_ADVANCED_PRECIPITATE_GENERATOR) {
                event.accept(ModBlocks.ADVANCED_PRECIPITATE_GENERATOR_ITEM.get());
            }
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WHITE_SOCK.get());
            event.accept(ModItems.RAINBOW_WHITE_SOCK.get());
            event.accept(ModItems.TRAVEL_DISPOSABLE_SOCK.get());
            event.accept(ModItems.BOAT_SOCK.get().getDefaultInstance());
            event.accept(ModItems.OVER_KNEE_SOCK.get());
            event.accept(ModItems.SPORT_CREW_SOCK.get());
            event.accept(ModItems.PANTYHOSE.get());
            event.accept(ModItems.SPLIT_TOE_SOCK.get());
            event.accept(ModItems.STOCKINGS.get());
            event.accept(ModItems.ZHAOZHAO_ORIGINAL_SCENT.get());
            event.accept(ModItems.CHESED_ORIGINAL_SCENT.get());
            event.accept(ModItems.TEST_CHEESE.get());
            event.accept(ModItems.FRESH_PRESSED_CHEESE.get());
            event.accept(ModItems.CONCENTRATED_FRESH_PRESSED_CHEESE_BUCKET.get());
            event.accept(ModItems.DILUTED_FRESH_PRESSED_CHEESE_BUCKET.get());
            event.accept(ModItems.BURNING_BANANA.get());
            event.accept(ModItems.COLORFUL_BURNING_BANANA.get());
            event.accept(ModItems.COLORFUL_BANANA_SLICE.get());
            event.accept(ModItems.DIRTY_WHITE_SOCK.get());
            event.accept(ModItems.STIR_FRIED_SOCK.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.BURNING_BANANA.get());
            event.accept(ModItems.COLORFUL_BURNING_BANANA.get());
            event.accept(ModItems.COLORFUL_BANANA_SLICE.get());
            if (ModList.get().isLoaded(KaleidoscopeTavernCompat.MOD_ID)) {
                event.accept(KaleidoscopeTavernCompat.createMaxQualityStack());
            }
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModBlocks.BANANA_POOP_ITEM.get());
        }
    }

}
