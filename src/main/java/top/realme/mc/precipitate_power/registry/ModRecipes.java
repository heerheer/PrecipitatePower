package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.recipe.ElectricSockUpgradeRecipe;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, PrecipitatePower.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ElectricSockUpgradeRecipe>> SMALL_TO_MEDIUM_ELECTRIC_SOCK =
            REGISTER.register("small_to_medium_electric_sock", () ->
                    new SimpleCraftingRecipeSerializer<>(category ->
                            new ElectricSockUpgradeRecipe(category, ElectricSockUpgradeRecipe.Upgrade.MEDIUM)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ElectricSockUpgradeRecipe>> MEDIUM_TO_LARGE_ELECTRIC_SOCK =
            REGISTER.register("medium_to_large_electric_sock", () ->
                    new SimpleCraftingRecipeSerializer<>(category ->
                            new ElectricSockUpgradeRecipe(category, ElectricSockUpgradeRecipe.Upgrade.LARGE)));

    private ModRecipes() {
    }
}
