package top.realme.mc.precipitate_power.registry;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.loot.AddSockLootModifier;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PrecipitatePower.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddSockLootModifier>> ADD_SOCK_LOOT =
            REGISTER.register("add_sock_loot", () -> AddSockLootModifier.CODEC);

    private ModLootModifiers() {
    }
}
