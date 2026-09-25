package top.realme.mc.precipitate_power.menu;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import top.realme.mc.precipitate_power.item.ElectricSockItem;
import top.realme.mc.precipitate_power.util.EnergyTransferUtil;

public final class ElectricSockMenu {
    private static final String UI_RESOURCE = "/assets/precipitate_power/ui/electric_sock.xml";

    private ElectricSockMenu() {
    }

    public static ModularUI createUI(ItemStack sockStack, Player player) {
        if (!(sockStack.getItem() instanceof ElectricSockItem sockItem)) {
            throw new IllegalArgumentException("Electric sock UI requires an ElectricSockItem");
        }

        UI ui = loadUI();
        ComponentItemHandler handler = sockItem.createItemHandler(sockStack);
        bindItemSlot(ui, "internal-slot-1", new ItemHandlerCopySlot(handler, 0, 62, 61));
        bindItemSlot(ui, "internal-slot-2", new ItemHandlerCopySlot(handler, 1, 98, 61));

        bindLabel(ui, "title-label", sockStack::getHoverName);
        bindLabel(ui, "energy-label", () -> {
            IEnergyStorage storage = sockItem.createEnergyStorage(sockStack);
            return Component.translatable("gui.precipitate_power.electric_sock.energy",
                    storage.getEnergyStored(), storage.getMaxEnergyStored());
        });
        bindLabel(ui, "output-label", () -> Component.translatable(
                "gui.precipitate_power.electric_sock.output", sockItem.getTransferRate()));

        bindProgressMeter(ui, "energy-meter", "energy-fill", "energy-bar", () -> {
            IEnergyStorage storage = sockItem.createEnergyStorage(sockStack);
            return storage.getMaxEnergyStored() <= 0
                    ? 0.0F
                    : storage.getEnergyStored() / (float) storage.getMaxEnergyStored();
        });

        UIElement root = requireElement(ui, "electric-sock-root", UIElement.class);
        // LDLib2 2.2.1 serverTick reads both listener phases whenever either phase exists.
        root.addServerEventListener(UIEvents.TICK, event -> {
        }, true);
        root.addServerEventListener(UIEvents.TICK, event -> {
            if (!player.level().isClientSide()) {
                chargeInternalItems(sockStack, sockItem, handler);
            }
        });
        return ModularUI.of(ui, player);
    }

    private static void chargeInternalItems(ItemStack sockStack, ElectricSockItem sockItem, ComponentItemHandler handler) {
        IEnergyStorage source = sockItem.createEnergyStorage(sockStack);
        int budget = Math.min(sockItem.getTransferRate(), source.getEnergyStored());
        if (budget <= 0) {
            return;
        }

        List<ChargeTarget> targets = new ArrayList<>(ElectricSockItem.INTERNAL_SLOTS);
        for (int slot = 0; slot < ElectricSockItem.INTERNAL_SLOTS; slot++) {
            ItemStack target = handler.getStackInSlot(slot);
            int capacity = EnergyTransferUtil.getReceivableEnergy(target, budget);
            if (capacity > 0) {
                targets.add(new ChargeTarget(slot, target, capacity));
            }
        }
        if (targets.isEmpty()) {
            return;
        }

        int[] allocations = allocateFairly(targets, budget);
        int transferred = 0;
        for (int i = 0; i < targets.size(); i++) {
            if (allocations[i] <= 0) {
                continue;
            }
            ChargeTarget target = targets.get(i);
            int accepted = EnergyTransferUtil.chargeItemStack(target.stack(), allocations[i]);
            if (accepted > 0) {
                handler.setStackInSlot(target.slot(), target.stack());
                transferred += accepted;
            }
        }
        if (transferred > 0) {
            source.extractEnergy(transferred, false);
        }
    }

    private static int[] allocateFairly(List<ChargeTarget> targets, int budget) {
        int[] allocations = new int[targets.size()];
        List<Integer> active = new ArrayList<>(targets.size());
        for (int i = 0; i < targets.size(); i++) {
            active.add(i);
        }

        int remaining = budget;
        while (remaining > 0 && !active.isEmpty()) {
            int share = Math.max(1, remaining / active.size());
            boolean removedLimitedTarget = false;
            for (int index = active.size() - 1; index >= 0 && remaining > 0; index--) {
                int targetIndex = active.get(index);
                int targetRemaining = targets.get(targetIndex).capacity() - allocations[targetIndex];
                int allocated = Math.min(remaining, Math.min(share, targetRemaining));
                allocations[targetIndex] += allocated;
                remaining -= allocated;
                if (allocations[targetIndex] >= targets.get(targetIndex).capacity()) {
                    active.remove(index);
                    removedLimitedTarget = true;
                }
            }
            if (!removedLimitedTarget && remaining < active.size()) {
                for (int i = 0; i < remaining; i++) {
                    allocations[active.get(i)]++;
                }
                remaining = 0;
            }
        }
        return allocations;
    }

    private static UI loadUI() {
        try (var stream = ElectricSockMenu.class.getResourceAsStream(UI_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing electric sock UI XML: " + UI_RESOURCE);
            }
            var document = XmlUtils.loadXml(stream);
            if (document == null) {
                throw new IllegalStateException("Invalid electric sock UI XML: " + UI_RESOURCE);
            }
            return UI.of(document);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load electric sock UI XML: " + UI_RESOURCE, exception);
        }
    }

    private static void bindItemSlot(UI ui, String id, Slot slot) {
        requireElement(ui, id, ItemSlot.class).bind(slot);
    }

    private static void bindLabel(UI ui, String id, java.util.function.Supplier<Component> supplier) {
        requireElement(ui, id, Label.class).bind(DataBindingBuilder.componentS2C(supplier).build());
    }

    private static void bindProgressMeter(UI ui, String meterId, String fillId, String dataBarId,
                                          java.util.function.Supplier<Float> progressSupplier) {
        UIElement meter = requireElement(ui, meterId, UIElement.class);
        UIElement fill = requireElement(ui, fillId, UIElement.class);
        ProgressBar dataBar = requireElement(ui, dataBarId, ProgressBar.class);
        dataBar.setMinValue(0.0F);
        dataBar.setMaxValue(1.0F);
        dataBar.setVisible(false);
        dataBar.bind(DataBindingBuilder.floatValS2C(progressSupplier).build());
        meter.addEventListener(UIEvents.TICK, event -> fill.layout(layout ->
                layout.widthPercent(Mth.clamp(dataBar.getValue(), 0.0F, 1.0F) * 100.0F)));
    }

    private static <T extends UIElement> T requireElement(UI ui, String id, Class<T> type) {
        return ui.selectId(id, type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing LDLib2 XML UI element: " + id));
    }

    private record ChargeTarget(int slot, ItemStack stack, int capacity) {
    }
}
