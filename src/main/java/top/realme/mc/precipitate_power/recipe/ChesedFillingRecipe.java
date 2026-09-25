package top.realme.mc.precipitate_power.recipe;

import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.item.ChesedOriginalScentItem;

public final class ChesedFillingRecipe extends FillingRecipe {
    public ChesedFillingRecipe(ProcessingRecipeParams params) {
        super(params);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.getItem(0);
        return stack.getItem() instanceof ChesedOriginalScentItem
                && ChesedOriginalScentItem.getData(stack).canFeed()
                && super.matches(input, level);
    }
}
