package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(Registries.SOUND_EVENT, PrecipitatePower.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SOCK_OFFERING = REGISTER.register(
            "sock_offering",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    PrecipitatePower.MODID, "sock_offering")));

    private ModSounds() {
    }
}
