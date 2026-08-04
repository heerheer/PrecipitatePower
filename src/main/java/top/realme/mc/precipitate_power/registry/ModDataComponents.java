package top.realme.mc.precipitate_power.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.item.ChesedSockData;
import top.realme.mc.precipitate_power.item.BurningBananaData;

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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChesedSockData>> CHESED_SOCK_DATA =
            REGISTER.registerComponentType("chesed_sock_data", builder -> builder
                    .persistent(ChesedSockData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(ChesedSockData.CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> CHESED_FEEDING_CHEESE =
            REGISTER.registerComponentType("chesed_feeding_cheese", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BurningBananaData>> BURNING_BANANA_DATA =
            REGISTER.registerComponentType("burning_banana_data", builder -> builder
                    .persistent(BurningBananaData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(BurningBananaData.CODEC)));

    private ModDataComponents() {
    }
}
