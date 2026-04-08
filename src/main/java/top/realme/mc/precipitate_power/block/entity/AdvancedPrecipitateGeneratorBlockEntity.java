package top.realme.mc.precipitate_power.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModBlockEntities;

public class AdvancedPrecipitateGeneratorBlockEntity extends AbstractPrecipitateGeneratorBlockEntity {
    private static final String WATER_AMOUNT_TAG = "WaterAmount";

    private final WaterTankHandler fluidHandler = new WaterTankHandler();
    private int waterAmount;

    public AdvancedPrecipitateGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ADVANCED_PRECIPITATE_GENERATOR.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedPrecipitateGeneratorBlockEntity blockEntity) {
        blockEntity.tickServer();
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return fluidHandler;
    }

    @Override
    protected double getGenerationMultiplier() {
        return 2.0D;
    }

    @Override
    protected double getDirtyChanceMultiplier() {
        return 0.5D;
    }

    @Override
    protected boolean canConsumeGenerationResource(int precipitation) {
        return waterAmount >= getWaterConsumption(precipitation);
    }

    @Override
    protected void consumeGenerationResource(int precipitation) {
        int cost = getWaterConsumption(precipitation);
        if (cost > 0) {
            waterAmount = Math.max(0, waterAmount - cost);
        }
    }

    @Override
    protected int getWaterStored() {
        return waterAmount;
    }

    @Override
    protected int getWaterCapacity() {
        return Config.ADVANCED_GENERATOR_WATER_CAPACITY.get();
    }

    @Override
    protected void loadGeneratorData(CompoundTag tag, HolderLookup.Provider registries) {
        waterAmount = Math.max(0, Math.min(getWaterCapacity(), tag.getInt(WATER_AMOUNT_TAG)));
    }

    @Override
    protected void saveGeneratorData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(WATER_AMOUNT_TAG, waterAmount);
    }

    @Override
    protected String getContainerTranslationKey() {
        return "block.precipitate_power.advanced_precipitate_generator";
    }

    private int getWaterConsumption(int precipitation) {
        return Config.ADVANCED_GENERATOR_BASE_WATER_USE.get()
                + precipitation * Config.ADVANCED_GENERATOR_WATER_USE_PER_PRECIPITATION.get();
    }

    private final class WaterTankHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 && waterAmount > 0 ? new FluidStack(Fluids.WATER, waterAmount) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? getWaterCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid() == Fluids.WATER;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource) || resource.isEmpty()) {
                return 0;
            }

            int accepted = Math.min(getWaterCapacity() - waterAmount, resource.getAmount());
            if (accepted > 0 && action.execute()) {
                waterAmount += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || resource.getFluid() != Fluids.WATER) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || waterAmount <= 0) {
                return FluidStack.EMPTY;
            }

            int drained = Math.min(waterAmount, maxDrain);
            FluidStack stack = new FluidStack(Fluids.WATER, drained);
            if (action.execute()) {
                waterAmount -= drained;
                setChanged();
            }
            return stack;
        }
    }
}
