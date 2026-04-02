package top.realme.mc.precipitate_power.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.block.entity.PrecipitateGeneratorBlockEntity;
import top.realme.mc.precipitate_power.registry.ModMenus;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class PrecipitateGeneratorMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public PrecipitateGeneratorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, new SimpleContainer(2), new SimpleContainerData(4));
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
