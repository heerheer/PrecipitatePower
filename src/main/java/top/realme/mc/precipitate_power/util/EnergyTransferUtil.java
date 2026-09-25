package top.realme.mc.precipitate_power.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class EnergyTransferUtil {
    private EnergyTransferUtil() {
    }

    public static int getReceivableEnergy(ItemStack target, int limit) {
        IEnergyStorage storage = getReceivingStorage(target);
        return storage == null || limit <= 0 ? 0 : storage.receiveEnergy(limit, true);
    }

    public static int chargeItemStack(ItemStack target, int amount) {
        IEnergyStorage storage = getReceivingStorage(target);
        return storage == null || amount <= 0 ? 0 : storage.receiveEnergy(amount, false);
    }

    private static IEnergyStorage getReceivingStorage(ItemStack target) {
        if (target.isEmpty()) {
            return null;
        }
        IEnergyStorage storage = target.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage != null && storage.canReceive() ? storage : null;
    }
}
