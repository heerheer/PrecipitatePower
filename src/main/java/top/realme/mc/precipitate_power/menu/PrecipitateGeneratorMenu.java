package top.realme.mc.precipitate_power.menu;

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.block.entity.AbstractPrecipitateGeneratorBlockEntity;
import top.realme.mc.precipitate_power.registry.ModMenus;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class PrecipitateGeneratorMenu extends AbstractContainerMenu {
    private static final ResourceLocation GENERATOR_UI_XML = ResourceLocation.fromNamespaceAndPath("ldlib2", "ui/precipitate_generator.xml");

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
        return createUI(
                player,
                slots.get(0),
                slots.get(1),
                this::getEnergyStored,
                this::getMaxEnergyStored,
                this::getMaxExtract,
                this::getExtraMaxExtract,
                this::getWaterStored,
                this::getMaxWaterStored,
                this::getPrecipitationLevel,
                this::getDirtyCount
        );
    }

    public static ModularUI createUI(AbstractPrecipitateGeneratorBlockEntity generator, Player player) {
        Slot inputSlot = new Slot(generator, 0, 44, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SockDataUtil.isGeneratorSock(stack);
            }
        };
        Slot outputSlot = new Slot(generator, 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
        return createUI(
                player,
                inputSlot,
                outputSlot,
                () -> generator.getData().get(0),
                () -> generator.getData().get(1),
                () -> generator.getData().get(6),
                () -> generator.getData().get(7),
                () -> generator.getData().get(4),
                () -> generator.getData().get(5),
                () -> generator.getData().get(2),
                () -> generator.getData().get(3)
        );
    }

    private static ModularUI createUI(Player player,
                                      Slot inputSlot,
                                      Slot outputSlot,
                                      java.util.function.Supplier<Integer> energyStored,
                                      java.util.function.Supplier<Integer> maxEnergyStored,
                                      java.util.function.Supplier<Integer> maxExtract,
                                      java.util.function.Supplier<Integer> extraMaxExtract,
                                      java.util.function.Supplier<Integer> waterStored,
                                      java.util.function.Supplier<Integer> maxWaterStored,
                                      java.util.function.Supplier<Integer> precipitationLevel,
                                      java.util.function.Supplier<Integer> dirtyCount) {
        UI ui = UI.of(XmlUtils.loadXml(GENERATOR_UI_XML));

        bindItemSlot(ui, "machine-input-slot", inputSlot);
        bindItemSlot(ui, "machine-output-slot", outputSlot);

        bindLabel(ui, "energy-label",
                () -> Component.translatable("gui.precipitate_power.energy", energyStored.get(), maxEnergyStored.get()));
        bindLabel(ui, "max-extract-label",
                () -> Component.translatable("gui.precipitate_power.max_extract", maxExtract.get(), extraMaxExtract.get()));
        bindLabel(ui, "water-label",
                () -> maxWaterStored.get() > 0
                        ? Component.translatable("gui.precipitate_power.water", waterStored.get(), maxWaterStored.get())
                        : Component.empty());
        bindLabel(ui, "precipitation-label",
                () -> Component.translatable("gui.precipitate_power.precipitation", precipitationLevel.get()));
        bindLabel(ui, "dirty-count-label",
                () -> Component.translatable("gui.precipitate_power.dirty_count", dirtyCount.get()));

        bindProgressBar(ui, "energy-bar", 0xFF46C266,
                () -> maxEnergyStored.get() <= 0 ? 0.0F : energyStored.get() / (float) maxEnergyStored.get(),
                () -> Component.translatable("gui.precipitate_power.energy", energyStored.get(), maxEnergyStored.get()),
                true);
        bindProgressBar(ui, "water-bar", 0xFF3B82F6,
                () -> maxWaterStored.get() <= 0 ? 0.0F : waterStored.get() / (float) maxWaterStored.get(),
                () -> Component.translatable("gui.precipitate_power.water", waterStored.get(), maxWaterStored.get()),
                maxWaterStored.get() > 0);

        return ModularUI.of(ui, player);
    }

    private static void bindItemSlot(UI ui, String id, Slot slot) {
        ItemSlot itemSlot = requireElement(ui, id, ItemSlot.class);
        itemSlot.bind(slot);
    }

    private static void bindLabel(UI ui, String id, java.util.function.Supplier<Component> supplier) {
        Label label = requireElement(ui, id, Label.class);
        label.bind(DataBindingBuilder.componentS2C(supplier).build());
    }

    private static void bindProgressBar(UI ui, String id, int fillColor,
                                        java.util.function.Supplier<Float> progressSupplier,
                                        java.util.function.Supplier<Component> tooltipSupplier,
                                        boolean visible) {
        ProgressBar bar = requireElement(ui, id, ProgressBar.class);
        bar.setVisible(visible);
        bar.setMinValue(0.0F);
        bar.setMaxValue(1.0F);
        bar.bind(DataBindingBuilder.floatValS2C(progressSupplier).build());
        bar.barContainer(container -> container.style(style -> style.background(new ColorRectTexture(0xFF16181D))));
        bar.bar(inner -> inner.style(style -> style.background(new ColorRectTexture(fillColor)).tooltips(tooltipSupplier.get())));
    }

    private static <T extends UIElement> T requireElement(UI ui, String id, Class<T> type) {
        return ui.selectId(id, type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing LDLib2 XML UI element: " + id));
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
