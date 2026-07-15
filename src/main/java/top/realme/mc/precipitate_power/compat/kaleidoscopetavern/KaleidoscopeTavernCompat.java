package top.realme.mc.precipitate_power.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.mixin.BlockEntityTypeAccessor;

public final class KaleidoscopeTavernCompat {
    public static final String MOD_ID = "kaleidoscope_tavern";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PrecipitatePower.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PrecipitatePower.MODID);

    private static final DeferredBlock<DrinkBlock> ORIGINAL_BREW_BLOCK = BLOCKS.register(
            "original_brew",
            () -> new DrinkBlock(4)
    );

    public static final DeferredItem<OriginalBrewItem> ORIGINAL_BREW = ITEMS.register(
            "original_brew",
            () -> new OriginalBrewItem(ORIGINAL_BREW_BLOCK.get())
    );

    private KaleidoscopeTavernCompat() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(KaleidoscopeTavernCompat::onCommonSetup);
    }

    public static ItemStack createMaxQualityStack() {
        ItemStack stack = ORIGINAL_BREW.get().getDefaultInstance();
        BottleBlockItem.setBrewLevel(stack, IBarrel.BREWING_FINISHED);
        return stack;
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) (Object) ModBlocks.DRINK_BE.get();
            Set<Block> validBlocks = new HashSet<>(accessor.precipitatePower$getValidBlocks());
            validBlocks.add(ORIGINAL_BREW_BLOCK.get());
            accessor.precipitatePower$setValidBlocks(Set.copyOf(validBlocks));
        });
    }
}
