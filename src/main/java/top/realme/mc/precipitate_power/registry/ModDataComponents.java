package top.realme.mc.precipitate_power.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PrecipitatePower.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ELECTRIC_SOCK_ENERGY =
            REGISTER.registerComponentType("electric_sock_energy", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> ELECTRIC_SOCK_INVENTORY =
            REGISTER.registerComponentType("electric_sock_inventory", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    private ModDataComponents() {
    }
}
