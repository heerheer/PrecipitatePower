package top.realme.mc.precipitate_power.menu;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.block.entity.SockBlenderBlockEntity;
import top.realme.mc.precipitate_power.item.SockBlendingIngredients;
import top.realme.mc.precipitate_power.registry.ModTags;

public final class SockBlenderMenu {
    private static final String UI_RESOURCE = "/assets/precipitate_power/ui/sock_blender.xml";

    private SockBlenderMenu() {
    }

    public static ModularUI createUI(SockBlenderBlockEntity blender, Player player) {
        UI ui = loadUI();

        bindItemSlot(ui, "sock-slot", new Slot(blender, SockBlenderBlockEntity.SOCK_SLOT, 35, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.SOCK);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        bindItemSlot(ui, "ingredient-slot", new Slot(blender, SockBlenderBlockEntity.INGREDIENT_SLOT, 123, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SockBlendingIngredients.findMaterials(stack).size() == 1;
            }
        });

        bindLabel(ui, "percentage-label", blender::getSelectedPercentText);
        bindLabel(ui, "current-blend-label", blender::getCurrentBlendText);
        bindLabel(ui, "ingredient-label", blender::getIngredientText);
        bindLabel(ui, "status-label", blender::getStatusText);

        bindServerButton(ui, "minus-ten-button", () -> blender.adjustSelectedPercent(-10));
        bindServerButton(ui, "minus-one-button", () -> blender.adjustSelectedPercent(-1));
        bindServerButton(ui, "plus-one-button", () -> blender.adjustSelectedPercent(1));
        bindServerButton(ui, "plus-ten-button", () -> blender.adjustSelectedPercent(10));
        bindServerButton(ui, "blend-button", () -> blender.tryBlend(player));

        return ModularUI.of(ui, player);
    }

    private static UI loadUI() {
        try (var stream = SockBlenderMenu.class.getResourceAsStream(UI_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing sock blender UI XML: " + UI_RESOURCE);
            }
            var document = XmlUtils.loadXml(stream);
            if (document == null) {
                throw new IllegalStateException("Invalid sock blender UI XML: " + UI_RESOURCE);
            }
            return UI.of(document);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load sock blender UI XML: " + UI_RESOURCE, exception);
        }
    }

    private static void bindItemSlot(UI ui, String id, Slot slot) {
        requireElement(ui, id, ItemSlot.class).bind(slot);
    }

    private static void bindLabel(UI ui, String id, Supplier<Component> supplier) {
        requireElement(ui, id, Label.class).bind(DataBindingBuilder.componentS2C(supplier).build());
    }

    private static void bindServerButton(UI ui, String id, Runnable action) {
        requireElement(ui, id, Button.class).setOnServerClick(event -> action.run());
    }

    private static <T extends UIElement> T requireElement(UI ui, String id, Class<T> type) {
        return ui.selectId(id, type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing LDLib2 XML UI element: " + id));
    }
}
