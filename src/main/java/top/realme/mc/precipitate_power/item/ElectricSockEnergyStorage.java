package top.realme.mc.precipitate_power.item;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import top.realme.mc.precipitate_power.registry.ModDataComponents;

final class ElectricSockEnergyStorage implements IEnergyStorage {
    private final ItemStack stack;
    private final int capacity;
    private final int transferRate;

    ElectricSockEnergyStorage(ItemStack stack, int capacity, int transferRate) {
        this.stack = stack;
        this.capacity = Math.max(0, capacity);
        this.transferRate = Math.max(0, transferRate);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Mth.clamp(capacity - getEnergyStored(), 0, Math.min(transferRate, maxReceive));
        if (!simulate && accepted > 0) {
            setEnergy(getEnergyStored() + accepted);
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(getEnergyStored(), Math.min(transferRate, Math.max(0, maxExtract)));
        if (!simulate && extracted > 0) {
            setEnergy(getEnergyStored() - extracted);
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return Mth.clamp(stack.getOrDefault(ModDataComponents.ELECTRIC_SOCK_ENERGY.get(), 0), 0, capacity);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return transferRate > 0;
    }

    @Override
    public boolean canReceive() {
        return transferRate > 0;
    }

    private void setEnergy(int energy) {
        stack.set(ModDataComponents.ELECTRIC_SOCK_ENERGY.get(), Mth.clamp(energy, 0, capacity));
    }
}
