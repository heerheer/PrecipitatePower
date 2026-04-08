package top.realme.mc.precipitate_power.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import top.realme.mc.precipitate_power.registry.ModBlockEntities;

public class PrecipitateGeneratorBlockEntity extends AbstractPrecipitateGeneratorBlockEntity {
    public PrecipitateGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PRECIPITATE_GENERATOR.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrecipitateGeneratorBlockEntity blockEntity) {
        blockEntity.tickServer();
    }

    @Override
    protected String getContainerTranslationKey() {
        return "block.precipitate_power.precipitate_generator";
    }
}
