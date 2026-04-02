package top.realme.mc.precipitate_power.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.ponder.scenes.DirtyWhiteSockPonderScenes;
import top.realme.mc.precipitate_power.ponder.scenes.PrecipitateGeneratorPonderScenes;

public class PrecipitatePowerPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return PrecipitatePower.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PrecipitateGeneratorPonderScenes.register(helper);
        DirtyWhiteSockPonderScenes.register(helper);
    }
}
