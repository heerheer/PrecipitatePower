package top.realme.mc.precipitate_power.compat.curios;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosApi.registerCurio(ModItems.WHITE_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.RAINBOW_WHITE_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.TRAVEL_DISPOSABLE_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.BOAT_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.DIRTY_WHITE_SOCK.get(), SockCurio.INSTANCE);
            PrecipitatePower.LOGGER.info("Enabled Curios compat for sock items");
        });
    }
}
