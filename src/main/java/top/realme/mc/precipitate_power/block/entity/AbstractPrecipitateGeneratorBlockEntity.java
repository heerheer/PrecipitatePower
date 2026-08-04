package top.realme.mc.precipitate_power.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.item.GeneratorFuelItem;
import top.realme.mc.precipitate_power.item.GeneratorTickContext;
import top.realme.mc.precipitate_power.item.GeneratorTickResult;
import top.realme.mc.precipitate_power.menu.PrecipitateGeneratorMenu;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.SockDataUtil;
import top.realme.mc.precipitate_power.util.EnergyTransferUtil;

public abstract class AbstractPrecipitateGeneratorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    protected static final int INPUT_SLOT = 0;
    protected static final int OUTPUT_SLOT = 1;
    protected static final int CHARGE_SLOT = 2;
    private static final String EXTRA_MAX_EXTRACT_TAG = "ExtraMaxExtract";
    private static final String EXTRA_CAPACITY_TAG = "ExtraCapacity";
    private static final String CHARGE_SEDIMENT_ENERGY_TAG = "ChargeSedimentEnergy";
    private static final int CHARGE_SEDIMENT_STEP_ENERGY = 10_000;
    private static final int CHARGE_SEDIMENT_RATE_BONUS = 100;
    private static final int MAX_CHARGE_TRANSFER_RATE = 1_000_000;
    private static final long MAX_CHARGE_SEDIMENT_ENERGY =
            (long) MAX_CHARGE_TRANSFER_RATE / CHARGE_SEDIMENT_RATE_BONUS * CHARGE_SEDIMENT_STEP_ENERGY;

    private static final int[] TOP_SLOTS = new int[]{INPUT_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{OUTPUT_SLOT};
    private static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage();
    private final InvWrapper internalItemHandler = new InvWrapper(this);
    private final SidedInvWrapper upwardItemHandler = new SidedInvWrapper(this, Direction.UP);
    private final SidedInvWrapper downwardItemHandler = new SidedInvWrapper(this, Direction.DOWN);
    private int extraMaxExtract;
    private int extraCapacity;
    private int lastChargeRate;
    private long chargeSedimentEnergy;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> SockDataUtil.getPrecipitationLevel(items.get(INPUT_SLOT));
                case 3 -> SockDataUtil.getDirtyCount(items.get(INPUT_SLOT));
                case 4 -> getWaterStored();
                case 5 -> getWaterCapacity();
                case 6 -> getMaxExtract();
                case 7 -> extraMaxExtract;
                case 8 -> getChargeTransferRate();
                case 9 -> lastChargeRate;
                case 10 -> getChargeSedimentProgress();
                case 11 -> CHARGE_SEDIMENT_STEP_ENERGY;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                energyStorage.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    protected AbstractPrecipitateGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    protected final void tickServer() {
        ItemStack stack = items.get(INPUT_SLOT);
        if (level instanceof ServerLevel serverLevel && stack.getItem() instanceof GeneratorFuelItem fuelItem) {
            GeneratorTickResult result = fuelItem.tickInGenerator(new GeneratorTickContext(serverLevel, this, stack));
            applyGeneratorTickResult(stack, result);
        }
        chargeSlottedItem();
        pushEnergyToNeighbors();
    }

    private void chargeSlottedItem() {
        lastChargeRate = 0;
        ItemStack target = items.get(CHARGE_SLOT);
        int available = Math.min(energyStorage.getEnergyStored(), getChargeTransferRate());
        if (target.isEmpty() || available <= 0) {
            return;
        }

        int accepted = EnergyTransferUtil.chargeItemStack(target, available);
        if (accepted > 0) {
            energyStorage.extractForCharging(accepted);
            lastChargeRate = accepted;
            addChargeSediment(accepted);
            setChanged();
        }
    }

    public int getChargeTransferRate() {
        long sedimentLevels = chargeSedimentEnergy / CHARGE_SEDIMENT_STEP_ENERGY;
        long transferRate = (long) Config.GENERATOR_TRANSFER_RATE.get()
                + sedimentLevels * CHARGE_SEDIMENT_RATE_BONUS;
        return (int) Math.min(MAX_CHARGE_TRANSFER_RATE, Math.max(0L, transferRate));
    }

    private int getChargeSedimentProgress() {
        if (getChargeTransferRate() >= MAX_CHARGE_TRANSFER_RATE) {
            return CHARGE_SEDIMENT_STEP_ENERGY;
        }
        return (int) (chargeSedimentEnergy % CHARGE_SEDIMENT_STEP_ENERGY);
    }

    private void addChargeSediment(int chargedEnergy) {
        if (chargedEnergy <= 0 || getChargeTransferRate() >= MAX_CHARGE_TRANSFER_RATE) {
            return;
        }
        chargeSedimentEnergy = Math.min(MAX_CHARGE_SEDIMENT_ENERGY, chargeSedimentEnergy + chargedEnergy);
    }

    private void applyGeneratorTickResult(ItemStack originalStack, GeneratorTickResult result) {
        if (!result.handledCompletely()) {
            return;
        }

        if (result.generatedEnergy() > 0) {
            int accepted = energyStorage.addGeneratedEnergy(result.generatedEnergy());
            if (accepted > 0) {
                setChanged();
            }
        }

        if (result.energyToConsume() > 0) {
            consumeStoredEnergy(result.energyToConsume());
        }

        if (result.inputReplacement() != originalStack) {
            items.set(INPUT_SLOT, result.inputReplacement().isEmpty() ? ItemStack.EMPTY : result.inputReplacement());
        }

        if (!result.outputToInsert().isEmpty()) {
            insertOutput(result.outputToInsert());
        }

        if (result.changed()) {
            setChanged();
        }
    }

    private void insertOutput(ItemStack stack) {
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, stack.copy());
            return;
        }
        if (ItemStack.isSameItemSameComponents(output, stack) && output.getCount() < output.getMaxStackSize()) {
            output.grow(Math.min(stack.getCount(), output.getMaxStackSize() - output.getCount()));
        }
    }

    public boolean canInsertOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack output = items.get(OUTPUT_SLOT);
        return output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, stack)
                && output.getCount() <= output.getMaxStackSize() - stack.getCount();
    }

    private void pushEnergyToNeighbors() {
        if (level == null || level.isClientSide || energyStorage.getEnergyStored() <= 0) {
            return;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (energyStorage.getEnergyStored() <= 0) {
                break;
            }

            BlockPos targetPos = worldPosition.relative(direction);
            IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, direction.getOpposite());
            if (targetStorage == null || !targetStorage.canReceive()) {
                continue;
            }

            int maxExtract = energyStorage.extractEnergy(getMaxExtract(), true);
            if (maxExtract <= 0) {
                break;
            }

            int accepted = targetStorage.receiveEnergy(maxExtract, false);
            if (accepted > 0) {
                energyStorage.extractEnergy(accepted, false);
                setChanged();
            }
        }
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IItemHandler getInternalItemHandler() {
        return internalItemHandler;
    }

    public IItemHandler getSidedItemHandler(Direction side) {
        return switch (side) {
            case UP -> upwardItemHandler;
            case DOWN -> downwardItemHandler;
            default -> internalItemHandler;
        };
    }

    public ContainerData getData() {
        return data;
    }

    public int getMaxExtract() {
        return Math.max(0, Config.GENERATOR_MAX_EXTRACT.get() + extraMaxExtract);
    }

    public int getMaxEnergyCapacity() {
        return Math.max(0, Config.GENERATOR_CAPACITY.get() + extraCapacity);
    }

    public boolean consumeStoredEnergy(int amount) {
        if (amount <= 0 || energyStorage.getEnergyStored() < amount) {
            return false;
        }
        energyStorage.extractEnergy(amount, false);
        setChanged();
        return true;
    }

    public int fillEnergyToCapacity() {
        int filled = energyStorage.addGeneratedEnergy(
                energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
        if (filled > 0) {
            setChanged();
        }
        return filled;
    }

    public void addExtraMaxExtract(int amount) {
        if (amount <= 0) {
            return;
        }
        extraMaxExtract = Math.max(0, extraMaxExtract + amount);
        setChanged();
    }

    public void addExtraCapacity(int amount) {
        if (amount <= 0) {
            return;
        }
        extraCapacity = Math.max(0, extraCapacity + amount);
        setChanged();
    }

    public void replaceInputWithDirtySock() {
        ItemStack dirtyStack = new ItemStack(ModItems.DIRTY_WHITE_SOCK.get(), 1);
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, dirtyStack);
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        } else if (ItemStack.isSameItemSameComponents(output, dirtyStack) && output.getCount() < output.getMaxStackSize()) {
            output.grow(1);
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        } else {
            items.set(INPUT_SLOT, dirtyStack);
        }
        setChanged();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(getContainerTranslationKey());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, i < items.size() ? items.get(i) : ItemStack.EMPTY);
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PrecipitateGeneratorMenu(containerId, inventory, this, data);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch (direction) {
            case UP -> TOP_SLOTS;
            case DOWN -> BOTTOM_SLOTS;
            default -> NO_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.UP && slot == INPUT_SLOT && SockDataUtil.isGeneratorSock(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && slot == OUTPUT_SLOT;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack previous = items.get(slot);
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        if (slot == INPUT_SLOT && previous != stack && level instanceof ServerLevel
                && stack.getItem() instanceof GeneratorFuelItem fuelItem) {
            fuelItem.onInsertedIntoGenerator(this, stack);
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT) {
            return SockDataUtil.isGeneratorSock(stack);
        }
        if (slot == CHARGE_SLOT) {
            return !stack.isEmpty();
        }
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        extraMaxExtract = Math.max(0, tag.getInt(EXTRA_MAX_EXTRACT_TAG));
        extraCapacity = Math.max(0, tag.getInt(EXTRA_CAPACITY_TAG));
        chargeSedimentEnergy = Math.max(0L, Math.min(MAX_CHARGE_SEDIMENT_ENERGY, tag.getLong(CHARGE_SEDIMENT_ENERGY_TAG)));
        energyStorage.setEnergy(tag.getInt("Energy"));
        loadGeneratorData(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt(EXTRA_MAX_EXTRACT_TAG, extraMaxExtract);
        tag.putInt(EXTRA_CAPACITY_TAG, extraCapacity);
        tag.putLong(CHARGE_SEDIMENT_ENERGY_TAG, chargeSedimentEnergy);
        saveGeneratorData(tag, registries);
    }

    public double getGenerationMultiplierForItems() {
        return 1.0D;
    }

    public double getDirtyChanceMultiplierForItems() {
        return 1.0D;
    }

    public boolean canConsumeGenerationResourceForItems(int precipitation) {
        return true;
    }

    public void consumeGenerationResourceForItems(int precipitation) {
    }

    protected int getWaterStored() {
        return 0;
    }

    protected int getWaterCapacity() {
        return 0;
    }

    protected void loadGeneratorData(CompoundTag tag, HolderLookup.Provider registries) {
    }

    protected void saveGeneratorData(CompoundTag tag, HolderLookup.Provider registries) {
    }

    protected abstract String getContainerTranslationKey();

    private final class GeneratorEnergyStorage extends EnergyStorage {
        private GeneratorEnergyStorage() {
            super(Config.GENERATOR_CAPACITY.get(), 0, Config.GENERATOR_MAX_EXTRACT.get());
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            if (!canExtract() || toExtract <= 0) {
                return 0;
            }

            int extracted = Math.min(this.energy, Math.min(getMaxExtract(), toExtract));
            if (!simulate) {
                this.energy -= extracted;
            }
            return extracted;
        }

        @Override
        public boolean canExtract() {
            return getMaxExtract() > 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return getMaxEnergyCapacity();
        }

        private void setEnergy(int energy) {
            this.energy = Math.max(0, Math.min(getMaxEnergyStored(), energy));
        }

        private int addGeneratedEnergy(int amount) {
            if (amount <= 0) {
                return 0;
            }
            int accepted = Math.min(getMaxEnergyStored() - this.energy, amount);
            this.energy += accepted;
            return accepted;
        }

        private int extractForCharging(int amount) {
            int extracted = Math.min(this.energy, Math.max(0, amount));
            this.energy -= extracted;
            return extracted;
        }
    }
}
