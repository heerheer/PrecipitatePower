package top.realme.mc.precipitate_power.menu;

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.appliedenergistics.yoga.YogaPositionType;
import top.realme.mc.precipitate_power.registry.ModMenus;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class PrecipitateGeneratorMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public PrecipitateGeneratorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, new SimpleContainer(2), new SimpleContainerData(8));
    }

    public PrecipitateGeneratorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.PRECIPITATE_GENERATOR_MENU.get(), containerId);
        this.container = container;
        this.data = data;

        addSlot(new Slot(container, 0, 44, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SockDataUtil.isGeneratorSock(stack);
            }
        });
        addSlot(new Slot(container, 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        addDataSlots(data);

        if (this instanceof IModularUIHolderMenu holder) {
            holder.setModularUI(createModularUI(inventory.player));
        }
    }

    private ModularUI createModularUI(Player player) {
        UIElement root = new UIElement();
        root.layout(layout -> layout.width(176).height(166));
        root.style(style -> style.background(new ColorRectTexture(0xFF1E2026)));

        root.addChildren(
                box(6, 17, 164, 54, 0xFF2A2D36),
                box(43, 34, 18, 18, 0xFF4B5160),
                box(115, 34, 18, 18, 0xFF4B5160),
                box(62, 18, 16, 52, 0xFF16181D),
                box(80, 18, 16, 52, 0xFF16181D),
                dynamicLabel(8, 6, () -> Component.translatable("gui.precipitate_power.energy", getEnergyStored(), getMaxEnergyStored())),
                dynamicLabel(8, 18, () -> Component.translatable("gui.precipitate_power.max_extract", getMaxExtract(), getExtraMaxExtract())),
                dynamicLabel(8, 30, () -> getMaxWaterStored() > 0
                        ? Component.translatable("gui.precipitate_power.water", getWaterStored(), getMaxWaterStored())
                        : Component.empty()),
                dynamicLabel(8, 42, () -> Component.translatable("gui.precipitate_power.precipitation", getPrecipitationLevel())),
                dynamicLabel(8, 54, () -> Component.translatable("gui.precipitate_power.dirty_count", getDirtyCount())),
                progressBar(82, 18, 12, 50, 0xFF46C266,
                        () -> getMaxEnergyStored() <= 0 ? 0.0F : getEnergyStored() / (float) getMaxEnergyStored(),
                        () -> Component.translatable("gui.precipitate_power.energy", getEnergyStored(), getMaxEnergyStored())),
                progressBar(64, 18, 12, 50, 0xFF3B82F6,
                        () -> getMaxWaterStored() <= 0 ? 0.0F : getWaterStored() / (float) getMaxWaterStored(),
                        () -> Component.translatable("gui.precipitate_power.water", getWaterStored(), getMaxWaterStored()))
        );

        return ModularUI.of(
                UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                player
        );
    }

    private static UIElement box(int left, int top, int width, int height, int color) {
        UIElement element = new UIElement();
        element.layout(layout -> layout.positionType(YogaPositionType.ABSOLUTE).left(left).top(top).width(width).height(height));
        element.style(style -> style.background(new ColorRectTexture(color)));
        return element;
    }

    private static Label dynamicLabel(int left, int top, java.util.function.Supplier<Component> supplier) {
        Label label = new Label();
        label.layout(layout -> layout.positionType(YogaPositionType.ABSOLUTE).left(left).top(top));
        label.bindDataSource(SupplierDataSource.of(supplier).frequency(1));
        return label;
    }

    private static ProgressBar progressBar(int left, int top, int width, int height, int fillColor,
                                           java.util.function.Supplier<Float> progressSupplier,
                                           java.util.function.Supplier<Component> tooltipSupplier) {
        ProgressBar bar = new ProgressBar();
        bar.layout(layout -> layout.positionType(YogaPositionType.ABSOLUTE).left(left).top(top).width(width).height(height));
        bar.setMinValue(0.0F);
        bar.setMaxValue(1.0F);
        bar.bindDataSource(SupplierDataSource.of(progressSupplier).frequency(1));
        bar.barContainer(container -> container.style(style -> style.background(new ColorRectTexture(0xFF16181D))));
        bar.bar(inner -> inner.style(style -> style.background(new ColorRectTexture(fillColor)).tooltips(tooltipSupplier.get())));
        return bar;
    }

    public int getEnergyStored() {
        return data.get(0);
    }

    public int getMaxEnergyStored() {
        return data.get(1);
    }

    public int getPrecipitationLevel() {
        return data.get(2);
    }

    public int getDirtyCount() {
        return data.get(3);
    }

    public int getWaterStored() {
        return data.get(4);
    }

    public int getMaxWaterStored() {
        return data.get(5);
    }

    public int getMaxExtract() {
        return data.get(6);
    }

    public int getExtraMaxExtract() {
        return data.get(7);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 2) {
                if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (SockDataUtil.isGeneratorSock(stack)) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(inventory, slot, 8 + slot * 18, 142));
        }
    }
}
