package top.realme.mc.precipitate_power.item;

import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.block.entity.AbstractPrecipitateGeneratorBlockEntity;

/** An item that can occupy the precipitate generator's fuel slot. */
public interface GeneratorFuelItem {
    GeneratorTickResult tickInGenerator(GeneratorTickContext context);

    default boolean canPrecipitateInGenerator(ItemStack stack) {
        return false;
    }

    default int applyPrecipitationRolls(GeneratorTickContext context, long attempts) {
        return 0;
    }

    default void onInsertedIntoGenerator(AbstractPrecipitateGeneratorBlockEntity generator, ItemStack stack) {
    }
}
