package top.realme.mc.precipitate_power.compat.jei;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.item.SockMaterial;

public record SockBlendingDisplayRecipe(SockMaterial material, List<ItemStack> ingredients) {
    public static final int WIDTH = 170;
    public static final int HEIGHT = 160;

    public SockBlendingDisplayRecipe {
        ingredients = List.copyOf(ingredients);
    }

    public ModularUI createModularUI() {
        UIElement root = new UIElement();
        root.layout(layout -> layout
                .widthPercent(100)
                .heightPercent(100)
                .paddingAll(5)
                .gapAll(3)
                .flexDirection(FlexDirection.COLUMN));
        root.addClass("panel_bg");

        Label materialLabel = wrappedLabel(Component.translatable(
                "jei.precipitate_power.sock_blending.material",
                Component.translatable("material.precipitate_power." + material.id())), 14);
        Label effectLabel = wrappedLabel(Component.translatable(
                "jei.precipitate_power.sock_blending.effect",
                Component.translatable("jei.precipitate_power.sock_blending.effect." + material.id())), 34);
        Label descriptionLabel = wrappedLabel(Component.translatable(
                "jei.precipitate_power.sock_blending.description",
                Component.translatable("jei.precipitate_power.sock_blending.description." + material.id())), 25);
        Label ingredientTitle = wrappedLabel(Component.translatable(
                "jei.precipitate_power.sock_blending.ingredients"), 10);

        UIElement ingredientGrid = new UIElement();
        ingredientGrid.layout(layout -> layout
                .widthPercent(100)
                .gapAll(1)
                .flexDirection(FlexDirection.ROW)
                .wrap(FlexWrap.WRAP));
        if (ingredients.isEmpty()) {
            ingredientGrid.addChild(wrappedLabel(Component.translatable(
                    "jei.precipitate_power.sock_blending.no_ingredients"), 12));
        } else {
            for (ItemStack stack : ingredients) {
                ItemSlot slot = new ItemSlot().setItem(stack.copy());
                slot.xeiRecipeIngredient(IngredientIO.INPUT).xeiRecipeSlot();
                ingredientGrid.addChild(slot);
            }
        }

        ScrollerView ingredientScroller = new ScrollerView();
        ingredientScroller.layout(layout -> layout.widthPercent(100).height(44));
        ingredientScroller.addScrollViewChild(ingredientGrid);

        root.addChildren(materialLabel, effectLabel, descriptionLabel, ingredientTitle, ingredientScroller);
        return ModularUI.of(UI.of(root, List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC))));
    }

    private static Label wrappedLabel(Component text, float height) {
        Label label = new Label();
        label.setText(text);
        label.layout(layout -> layout.widthPercent(100).height(height));
        label.textStyle(style -> style
                .adaptiveWidth(false)
                .adaptiveHeight(false)
                .textWrap(TextWrap.WRAP));
        return label;
    }
}
