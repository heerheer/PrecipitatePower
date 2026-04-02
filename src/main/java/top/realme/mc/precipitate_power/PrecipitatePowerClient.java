package top.realme.mc.precipitate_power;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.createmod.ponder.foundation.PonderIndex;
import top.realme.mc.precipitate_power.ponder.PrecipitatePowerPonderPlugin;
import top.realme.mc.precipitate_power.client.PrecipitateGeneratorScreen;
import top.realme.mc.precipitate_power.registry.ModMenus;

@Mod(value = PrecipitatePower.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PrecipitatePower.MODID, value = Dist.CLIENT)
public class PrecipitatePowerClient {
    public PrecipitatePowerClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new PrecipitatePowerPonderPlugin()));
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PRECIPITATE_GENERATOR_MENU.get(), PrecipitateGeneratorScreen::new);
    }
}
