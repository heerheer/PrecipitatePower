package top.realme.mc.precipitate_power;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.createmod.ponder.foundation.PonderIndex;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import top.realme.mc.precipitate_power.client.ChesedFeedingItemRenderer;
import top.realme.mc.precipitate_power.client.ChesedUpgradeRenderer;
import top.realme.mc.precipitate_power.client.BloodiPowerAreaRenderer;
import top.realme.mc.precipitate_power.client.BloodiProjectileRenderer;
import top.realme.mc.precipitate_power.client.SockProjectileRenderer;
import top.realme.mc.precipitate_power.client.SockOfferingAltarRenderer;
import top.realme.mc.precipitate_power.ponder.PrecipitatePowerPonderPlugin;
import top.realme.mc.precipitate_power.registry.ModEntities;
import top.realme.mc.precipitate_power.registry.ModItems;

@Mod(value = PrecipitatePower.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = PrecipitatePower.MODID, value = Dist.CLIENT)
public class PrecipitatePowerClient {
    public PrecipitatePowerClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new PrecipitatePowerPonderPlugin());
        });
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CHESED_UPGRADE.get(), ChesedUpgradeRenderer::new);
        event.registerEntityRenderer(ModEntities.GIANT_SOCK_PROJECTILE.get(),
                context -> new SockProjectileRenderer<>(context, 3.0F, 24.0F));
        event.registerEntityRenderer(ModEntities.HOMING_SOCK_PROJECTILE.get(),
                context -> new SockProjectileRenderer<>(context, 0.75F, 38.0F));
        event.registerEntityRenderer(ModEntities.BLOODI_PROJECTILE.get(), BloodiProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SOCK_OFFERING_PROJECTILE.get(), BloodiProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BLOODI_POWER_AREA.get(), BloodiPowerAreaRenderer::new);
        event.registerEntityRenderer(ModEntities.SOCK_OFFERING_ALTAR.get(), SockOfferingAltarRenderer::new);
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                SimpleCustomRenderer.create(ModItems.CHESED_ORIGINAL_SCENT.get(), new ChesedFeedingItemRenderer()),
                ModItems.CHESED_ORIGINAL_SCENT.get()
        );
    }
}
