package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.block.PrecipitateGeneratorBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(PrecipitatePower.MODID);

    public static final DeferredBlock<Block> PRECIPITATE_GENERATOR = REGISTER.register(
            "precipitate_generator",
            () -> new PrecipitateGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<BlockItem> PRECIPITATE_GENERATOR_ITEM =
            ModItems.REGISTER.registerSimpleBlockItem("precipitate_generator", PRECIPITATE_GENERATOR);

    private ModBlocks() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.PRECIPITATE_GENERATOR.get(),
                (blockEntity, context) -> context == Direction.UP || context == Direction.DOWN ? null : blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PRECIPITATE_GENERATOR.get(),
                (blockEntity, context) -> context == null ? blockEntity.getInternalItemHandler() : blockEntity.getSidedItemHandler(context));
    }
}
