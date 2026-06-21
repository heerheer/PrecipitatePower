package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.block.entity.AdvancedPrecipitateGeneratorBlockEntity;
import top.realme.mc.precipitate_power.block.entity.PrecipitateGeneratorBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PrecipitatePower.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrecipitateGeneratorBlockEntity>> PRECIPITATE_GENERATOR = REGISTER.register(
            "precipitate_generator",
            () -> BlockEntityType.Builder.of(PrecipitateGeneratorBlockEntity::new, ModBlocks.PRECIPITATE_GENERATOR.get()).build(null)
    );

    // Temporarily disabled with the advanced generator block registration.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedPrecipitateGeneratorBlockEntity>> ADVANCED_PRECIPITATE_GENERATOR =
            ModBlocks.REGISTER_ADVANCED_PRECIPITATE_GENERATOR ? REGISTER.register(
                    "advanced_precipitate_generator",
                    () -> BlockEntityType.Builder.of(AdvancedPrecipitateGeneratorBlockEntity::new, ModBlocks.ADVANCED_PRECIPITATE_GENERATOR.get()).build(null)
            ) : null;

    private ModBlockEntities() {
    }
}
