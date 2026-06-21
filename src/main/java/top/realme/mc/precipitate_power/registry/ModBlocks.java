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
import top.realme.mc.precipitate_power.block.AdvancedPrecipitateGeneratorBlock;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.block.PrecipitateGeneratorBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(PrecipitatePower.MODID);
    public static final boolean REGISTER_ADVANCED_PRECIPITATE_GENERATOR = false;

    public static final DeferredBlock<Block> PRECIPITATE_GENERATOR = REGISTER.register(
            "precipitate_generator",
            () -> new PrecipitateGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<BlockItem> PRECIPITATE_GENERATOR_ITEM =
            ModItems.REGISTER.registerSimpleBlockItem("precipitate_generator", PRECIPITATE_GENERATOR);

    // Temporarily disabled until the advanced generator gameplay/art is ready to ship.
    public static final DeferredBlock<Block> ADVANCED_PRECIPITATE_GENERATOR = REGISTER_ADVANCED_PRECIPITATE_GENERATOR ? REGISTER.register(
            "advanced_precipitate_generator",
            () -> new AdvancedPrecipitateGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops())
    ) : null;

    public static final DeferredItem<BlockItem> ADVANCED_PRECIPITATE_GENERATOR_ITEM = REGISTER_ADVANCED_PRECIPITATE_GENERATOR
            ? ModItems.REGISTER.registerSimpleBlockItem("advanced_precipitate_generator", ADVANCED_PRECIPITATE_GENERATOR)
            : null;

    private ModBlocks() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.PRECIPITATE_GENERATOR.get(),
                (blockEntity, context) -> context == Direction.UP || context == Direction.DOWN ? null : blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PRECIPITATE_GENERATOR.get(),
                (blockEntity, context) -> context == null ? blockEntity.getInternalItemHandler() : blockEntity.getSidedItemHandler(context));
        if (REGISTER_ADVANCED_PRECIPITATE_GENERATOR) {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ADVANCED_PRECIPITATE_GENERATOR.get(),
                    (blockEntity, context) -> context == Direction.UP || context == Direction.DOWN ? null : blockEntity.getEnergyStorage());
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ADVANCED_PRECIPITATE_GENERATOR.get(),
                    (blockEntity, context) -> context == null ? blockEntity.getInternalItemHandler() : blockEntity.getSidedItemHandler(context));
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ADVANCED_PRECIPITATE_GENERATOR.get(),
                    (blockEntity, context) -> blockEntity.getFluidHandler(context));
        }
    }
}
