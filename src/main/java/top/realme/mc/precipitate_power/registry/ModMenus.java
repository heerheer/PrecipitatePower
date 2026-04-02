package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.menu.PrecipitateGeneratorMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER =
            DeferredRegister.create(Registries.MENU, PrecipitatePower.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PrecipitateGeneratorMenu>> PRECIPITATE_GENERATOR_MENU =
            REGISTER.register("precipitate_generator", () -> IMenuTypeExtension.create(PrecipitateGeneratorMenu::new));

    private ModMenus() {
    }
}
