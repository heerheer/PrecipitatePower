package top.realme.mc.precipitate_power.item;

import net.minecraft.world.item.ItemStack;

public record GeneratorTickResult(
        int generatedEnergy,
        int energyToConsume,
        boolean changed,
        ItemStack inputReplacement,
        ItemStack outputToInsert,
        boolean handledCompletely
) {
    public static final GeneratorTickResult PASS = new GeneratorTickResult(0, 0, false, ItemStack.EMPTY, ItemStack.EMPTY, false);

    public static GeneratorTickResult handled(int generatedEnergy, int energyToConsume, boolean changed, ItemStack inputReplacement, ItemStack outputToInsert) {
        return new GeneratorTickResult(generatedEnergy, energyToConsume, changed, inputReplacement, outputToInsert, true);
    }
}
