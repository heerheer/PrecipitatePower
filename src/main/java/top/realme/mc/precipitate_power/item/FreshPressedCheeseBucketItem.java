package top.realme.mc.precipitate_power.item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.FormulaParser;
import top.realme.mc.precipitate_power.util.SockDataUtil;

/** The concentrated cheese bucket is both placeable fluid and a consumable generator fuel. */
public final class FreshPressedCheeseBucketItem extends BucketItem implements GeneratorFuelItem {
    public FreshPressedCheeseBucketItem(Fluid fluid, Item.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        ItemStack stack = context.inputStack();
        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        int generated = (int) Math.max(0, Math.floor(
                FormulaParser.evaluate(Config.GENERATION_FORMULA.get(), precipitation)
                        * context.generationMultiplier()));
        boolean changed = false;

        if (generated > 0 && context.canConsumeGenerationResource(precipitation)) {
            context.consumeGenerationResource(precipitation);
            changed = true;
        } else {
            generated = 0;
        }

        if (context.level().getGameTime() % 20L != 0L) {
            return GeneratorTickResult.handled(generated, 0, changed, stack, ItemStack.EMPTY);
        }

        if (context.random().nextDouble() < Config.PRECIPITATE_CHANCE.get()) {
            SockDataUtil.addPrecipitation(stack, 1);
            precipitation++;
            changed = true;
        }

        ItemStack cheeseOutput = new ItemStack(ModItems.FRESH_PRESSED_CHEESE.get(), precipitation);
        if (precipitation > 0 && context.canInsertOutput(cheeseOutput)
                && context.random().nextDouble() < precipitation / 100.0D) {
            return GeneratorTickResult.handled(generated, 0, true, ItemStack.EMPTY,
                    cheeseOutput);
        }

        return GeneratorTickResult.handled(generated, 0, changed, stack, ItemStack.EMPTY);
    }
}
