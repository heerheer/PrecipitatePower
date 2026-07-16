package top.realme.mc.precipitate_power.menu;

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
    private static final String GENERATOR_UI_RESOURCE = "/assets/precipitate_power/ui/precipitate_generator.xml";

    private final Container container;
    private final ContainerData data;

    public PrecipitateGeneratorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, new SimpleContainer(3), new SimpleContainerData(12));
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
        addSlot(new Slot(container, 2, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty();
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
                slots.get(2),
                this::getEnergyStored,
                this::getMaxEnergyStored,
                this::getMaxExtract,
                this::getExtraMaxExtract,
                this::getWaterStored,
                this::getMaxWaterStored,
                this::getPrecipitationLevel,
                this::getDirtyCount,
                this::getTransferRate,
                this::getCurrentChargeRate,
                this::getChargeSedimentProgress,
                this::getChargeSedimentTarget
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
        Slot chargeSlot = new Slot(generator, 2, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return generator.canPlaceItem(2, stack);
            }
        };
        return createUI(
                player,
                inputSlot,
                outputSlot,
                chargeSlot,
                () -> generator.getData().get(0),
                () -> generator.getData().get(1),
                () -> generator.getData().get(6),
                () -> generator.getData().get(7),
                () -> generator.getData().get(4),
                () -> generator.getData().get(5),
                () -> generator.getData().get(2),
                () -> generator.getData().get(3),
                () -> generator.getData().get(8),
                () -> generator.getData().get(9),
                () -> generator.getData().get(10),
                () -> generator.getData().get(11)
        );
    }

    private static ModularUI createUI(Player player,
                                      Slot inputSlot,
                                      Slot outputSlot,
                                      Slot chargeSlot,
                                      java.util.function.Supplier<Integer> energyStored,
                                      java.util.function.Supplier<Integer> maxEnergyStored,
                                      java.util.function.Supplier<Integer> maxExtract,
                                      java.util.function.Supplier<Integer> extraMaxExtract,
                                      java.util.function.Supplier<Integer> waterStored,
                                      java.util.function.Supplier<Integer> maxWaterStored,
                                      java.util.function.Supplier<Integer> precipitationLevel,
                                      java.util.function.Supplier<Integer> dirtyCount,
                                      java.util.function.Supplier<Integer> transferRate,
                                      java.util.function.Supplier<Integer> currentChargeRate,
                                      java.util.function.Supplier<Integer> chargeSedimentProgress,
                                      java.util.function.Supplier<Integer> chargeSedimentTarget) {
        UI ui = loadGeneratorUI();

        bindItemSlot(ui, "machine-input-slot", inputSlot);
        bindItemSlot(ui, "machine-output-slot", outputSlot);
        bindItemSlot(ui, "machine-charge-slot", chargeSlot);

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
        bindLabel(ui, "charge-rate-label",
                () -> Component.translatable("gui.precipitate_power.charge_rate", currentChargeRate.get(), transferRate.get()));
        bindLabel(ui, "charge-sediment-label",
                () -> Component.translatable("gui.precipitate_power.charge_sediment",
                        chargeSedimentProgress.get(), chargeSedimentTarget.get()));

        boolean hasWaterTank = maxWaterStored.get() > 0;
        requireElement(ui, "water-label", Label.class).setVisible(hasWaterTank);
        requireElement(ui, "water-meter", UIElement.class).setVisible(hasWaterTank);

        bindProgressMeter(ui, "energy-meter", "energy-fill", "energy-bar",
                () -> maxEnergyStored.get() <= 0 ? 0.0F : energyStored.get() / (float) maxEnergyStored.get(),
                () -> Component.translatable("gui.precipitate_power.energy", energyStored.get(), maxEnergyStored.get()));
        bindProgressMeter(ui, "water-meter", "water-fill", "water-bar",
                () -> maxWaterStored.get() <= 0 ? 0.0F : waterStored.get() / (float) maxWaterStored.get(),
                () -> Component.translatable("gui.precipitate_power.water", waterStored.get(), maxWaterStored.get()));

        return ModularUI.of(ui, player);
    }

    private static UI loadGeneratorUI() {
        try (var stream = PrecipitateGeneratorMenu.class.getResourceAsStream(GENERATOR_UI_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing generator UI XML: " + GENERATOR_UI_RESOURCE);
            }
            var document = XmlUtils.loadXml(stream);
            if (document == null) {
                throw new IllegalStateException("Invalid generator UI XML: " + GENERATOR_UI_RESOURCE);
            }
            return UI.of(document);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load generator UI XML: " + GENERATOR_UI_RESOURCE, exception);
        }
    }

    private static void bindItemSlot(UI ui, String id, Slot slot) {
        ItemSlot itemSlot = requireElement(ui, id, ItemSlot.class);
        itemSlot.bind(slot);
    }

    private static void bindLabel(UI ui, String id, java.util.function.Supplier<Component> supplier) {
        Label label = requireElement(ui, id, Label.class);
        label.bind(DataBindingBuilder.componentS2C(supplier).build());
    }

    private static void bindProgressMeter(UI ui, String meterId, String fillId, String dataBarId,
                                          java.util.function.Supplier<Float> progressSupplier,
                                          java.util.function.Supplier<Component> tooltipSupplier) {
        UIElement meter = requireElement(ui, meterId, UIElement.class);
        UIElement fill = requireElement(ui, fillId, UIElement.class);
        ProgressBar dataBar = requireElement(ui, dataBarId, ProgressBar.class);
        dataBar.setMinValue(0.0F);
        dataBar.setMaxValue(1.0F);
        dataBar.setVisible(false);
        dataBar.bind(DataBindingBuilder.floatValS2C(progressSupplier).build());
        dataBar.label(label -> label.bind(DataBindingBuilder.componentS2C(tooltipSupplier).build()));
        meter.addEventListener(UIEvents.TICK, event -> {
            fill.layout(layout ->
                    layout.widthPercent(Mth.clamp(dataBar.getValue(), 0.0F, 1.0F) * 100.0F));
            meter.style(style -> style.tooltips(dataBar.label.getValue()));
        });
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

    public int getTransferRate() {
        return data.get(8);
    }

    public int getCurrentChargeRate() {
        return data.get(9);
    }

    public int getChargeSedimentProgress() {
        return data.get(10);
    }

    public int getChargeSedimentTarget() {
        return data.get(11);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 3) {
                if (!moveItemStackTo(stack, 3, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (SockDataUtil.isGeneratorSock(stack)) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 2, 3, false)) {
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
