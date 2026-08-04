package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, PrecipitatePower.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, PrecipitatePower.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PrecipitatePower.MODID);

    public static final DeferredHolder<FluidType, FluidType> CONCENTRATED_FRESH_PRESSED_CHEESE_TYPE =
            FLUID_TYPES.register("concentrated_fresh_pressed_cheese", () -> cheeseType(1_100, 1_400, 0xFFF4F4EA));
    public static final DeferredHolder<FluidType, FluidType> DILUTED_FRESH_PRESSED_CHEESE_TYPE =
            FLUID_TYPES.register("diluted_fresh_pressed_cheese", () -> cheeseType(1_000, 1_100, 0xFFFCFCF9));

    public static final DeferredHolder<Fluid, FlowingFluid> CONCENTRATED_FRESH_PRESSED_CHEESE =
            FLUIDS.register("concentrated_fresh_pressed_cheese", () -> new BaseFlowingFluid.Source(concentratedProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CONCENTRATED_FRESH_PRESSED_CHEESE =
            FLUIDS.register("flowing_concentrated_fresh_pressed_cheese", () -> new BaseFlowingFluid.Flowing(concentratedProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> DILUTED_FRESH_PRESSED_CHEESE =
            FLUIDS.register("diluted_fresh_pressed_cheese", () -> new BaseFlowingFluid.Source(dilutedProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_DILUTED_FRESH_PRESSED_CHEESE =
            FLUIDS.register("flowing_diluted_fresh_pressed_cheese", () -> new BaseFlowingFluid.Flowing(dilutedProperties()));

    public static final DeferredBlock<LiquidBlock> CONCENTRATED_FRESH_PRESSED_CHEESE_BLOCK = BLOCKS.register(
            "concentrated_fresh_pressed_cheese",
            () -> new LiquidBlock(CONCENTRATED_FRESH_PRESSED_CHEESE.get(), BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER)));
    public static final DeferredBlock<LiquidBlock> DILUTED_FRESH_PRESSED_CHEESE_BLOCK = BLOCKS.register(
            "diluted_fresh_pressed_cheese",
            () -> new LiquidBlock(DILUTED_FRESH_PRESSED_CHEESE.get(), BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER)));

    private static final BaseFlowingFluid.Properties CONCENTRATED_PROPERTIES = new BaseFlowingFluid.Properties(
            CONCENTRATED_FRESH_PRESSED_CHEESE_TYPE,
            CONCENTRATED_FRESH_PRESSED_CHEESE,
            FLOWING_CONCENTRATED_FRESH_PRESSED_CHEESE)
            .block(CONCENTRATED_FRESH_PRESSED_CHEESE_BLOCK)
            .bucket(ModItems.CONCENTRATED_FRESH_PRESSED_CHEESE_BUCKET);
    private static final BaseFlowingFluid.Properties DILUTED_PROPERTIES = new BaseFlowingFluid.Properties(
            DILUTED_FRESH_PRESSED_CHEESE_TYPE,
            DILUTED_FRESH_PRESSED_CHEESE,
            FLOWING_DILUTED_FRESH_PRESSED_CHEESE)
            .block(DILUTED_FRESH_PRESSED_CHEESE_BLOCK)
            .bucket(ModItems.DILUTED_FRESH_PRESSED_CHEESE_BUCKET);

    private static BaseFlowingFluid.Properties concentratedProperties() {
        return CONCENTRATED_PROPERTIES;
    }

    private static BaseFlowingFluid.Properties dilutedProperties() {
        return DILUTED_PROPERTIES;
    }

    private static FluidType cheeseType(int density, int viscosity, int tint) {
        return new FluidType(FluidType.Properties.create().density(density).viscosity(viscosity)
                .sound(SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)) {
            @Override
            public void initializeClient(java.util.function.Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return ResourceLocation.withDefaultNamespace("block/water_still");
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return ResourceLocation.withDefaultNamespace("block/water_flow");
                    }

                    @Override
                    public int getTintColor() {
                        return tint;
                    }
                });
            }
        };
    }

    private ModFluids() {
    }
}
