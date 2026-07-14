package top.realme.mc.precipitate_power.compat.curios;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.realme.mc.precipitate_power.item.AbstractSockItem;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.SockDataUtil;
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
            CuriosApi.registerCurio(ModItems.OVER_KNEE_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.SPORT_CREW_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.PANTYHOSE.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.SPLIT_TOE_SOCK.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.STOCKINGS.get(), SockCurio.INSTANCE);
            CuriosApi.registerCurio(ModItems.DIRTY_WHITE_SOCK.get(), SockCurio.INSTANCE);
            PrecipitatePower.LOGGER.info("Enabled Curios compat for sock items");
        });
    }

    public static boolean hasEquippedMatching(LivingEntity entity, java.util.function.Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> !handler.findCurios(predicate).isEmpty())
                .orElse(false);
    }

    public static double getEquippedMaterialScalar(LivingEntity entity, SockMaterial target, java.util.function.ToDoubleFunction<SockMaterial> extractor) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.findCurios(stack -> stack.getItem() instanceof AbstractSockItem sockItem && sockItem.isWearableSock(stack)).stream()
                        .mapToDouble(result -> SockDataUtil.getMaterialScalar(result.stack(), target, extractor))
                        .sum())
                .orElse(0.0D);
    }

    public static List<ItemStack> getEquippedWearableSocks(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.findCurios(stack -> stack.getItem() instanceof AbstractSockItem sockItem && sockItem.isWearableSock(stack)).stream()
                        .map(result -> result.stack())
                        .toList())
                .orElse(List.of());
    }
}
