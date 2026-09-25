package top.realme.mc.precipitate_power.compat.jei;

import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.item.SockBlendingIngredients;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.registry.ModBlocks;

@JeiPlugin
@MethodsReturnNonnullByDefault
public final class PrecipitatePowerJeiPlugin implements IModPlugin {
    public static final RecipeType<SockBlendingDisplayRecipe> SOCK_BLENDING = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "sock_blending"),
            SockBlendingDisplayRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SockBlendingCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SockBlendingDisplayRecipe> recipes = SockMaterial.VALUES.stream()
                .map(material -> new SockBlendingDisplayRecipe(
                        material, SockBlendingIngredients.getIngredientStacks(material)))
                .filter(recipe -> !recipe.ingredients().isEmpty())
                .toList();
        registration.addRecipes(SOCK_BLENDING, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.SOCK_BLENDER_ITEM.get(), SOCK_BLENDING);
    }

    private static final class SockBlendingCategory extends ModularUIRecipeCategory<SockBlendingDisplayRecipe> {
        private final IDrawable icon;

        private SockBlendingCategory(IJeiHelpers helpers) {
            super(SockBlendingDisplayRecipe::createModularUI);
            icon = helpers.getGuiHelper().createDrawableItemLike(ModBlocks.SOCK_BLENDER_ITEM.get());
        }

        @Override
        public RecipeType<SockBlendingDisplayRecipe> getRecipeType() {
            return SOCK_BLENDING;
        }

        @Override
        public IDrawable getIcon() {
            return icon;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("jei.precipitate_power.sock_blending.title");
        }

        @Override
        public int getWidth() {
            return SockBlendingDisplayRecipe.WIDTH;
        }

        @Override
        public int getHeight() {
            return SockBlendingDisplayRecipe.HEIGHT;
        }
    }
}
