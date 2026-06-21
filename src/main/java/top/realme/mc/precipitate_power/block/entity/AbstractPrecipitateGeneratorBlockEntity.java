package top.realme.mc.precipitate_power.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
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
import org.joml.Math;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.menu.PrecipitateGeneratorMenu;
import top.realme.mc.precipitate_power.registry.ModEnchantments;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.FormulaParser;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public abstract class AbstractPrecipitateGeneratorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    protected static final int INPUT_SLOT = 0;
    protected static final int OUTPUT_SLOT = 1;
    private static final String EXTRA_MAX_EXTRACT_TAG = "ExtraMaxExtract";
    private static final String EXTRA_CAPACITY_TAG = "ExtraCapacity";
    private static final double ATHLETIC_COGNITION_LOSS_CHANCE = 0.5D;
    private static final double ATHLETIC_COGNITION_LOSS_AMOUNT = 0.01D;

    private static final int[] TOP_SLOTS = new int[]{INPUT_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{OUTPUT_SLOT};
    private static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage();
    private final InvWrapper internalItemHandler = new InvWrapper(this);
    private final SidedInvWrapper upwardItemHandler = new SidedInvWrapper(this, Direction.UP);
    private final SidedInvWrapper downwardItemHandler = new SidedInvWrapper(this, Direction.DOWN);
    private int extraMaxExtract;
    private int extraCapacity;

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
            return 8;
        }
    };

    protected AbstractPrecipitateGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    protected final void tickServer() {
        ItemStack stack = items.get(INPUT_SLOT);
        if (!SockDataUtil.isGeneratorSock(stack)) {
            pushEnergyToNeighbors();
            return;
        }

        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        int generated = calculateGeneration(stack, precipitation);
        if (generated > 0 && canConsumeGenerationResource(precipitation)) {
            int accepted = energyStorage.addGeneratedEnergy(generated);
            if (accepted > 0) {
                consumeGenerationResource(precipitation);
                setChanged();
            }
        }

        pushEnergyToNeighbors();

        if (level != null && level.getGameTime() % 20L == 0L && stack.is(ModItems.BOAT_SOCK.get())) {
            growCapacityWithBoatSock(stack);
        }

        if (level != null && level.getGameTime() % 20L == 0L && level.random.nextDouble() < Config.PRECIPITATE_CHANCE.get()) {
            SockDataUtil.addPrecipitation(stack, 1);
            setChanged();
        }

        applyDirtyLogic(stack, precipitation);
    }

    private int calculateGeneration(ItemStack stack, int precipitation) {
        double coefficient = SockDataUtil.getPowerCoefficient(stack);
        double baseGeneration = FormulaParser.evaluate(Config.GENERATION_FORMULA.get(), precipitation);
        double multiplier = getGenerationMultiplier();
        if (stack.is(ModItems.TRAVEL_DISPOSABLE_SOCK.get())) {
            multiplier *= Config.TRAVEL_SOCK_GENERATION_MULTIPLIER.get();
        }
        return (int) Math.max(0, Math.floor(baseGeneration * coefficient * multiplier));
    }

    private void applyDirtyLogic(ItemStack stack, int precipitation) {
        if (level == null || SockDataUtil.isUnbreakable(stack)) {
            return;
        }

        if (stack.is(ModItems.BOAT_SOCK.get())) {
            return;
        }

        double dirtyChance = Config.DIRTY_BASE_CHANCE.get() + precipitation * Config.DIRTY_CHANCE_PER_PRECIPITATION.get();
        dirtyChance *= getDirtyChanceMultiplier();
        if (level.random.nextDouble() >= dirtyChance) {
            return;
        }

        if (stack.is(ModItems.TRAVEL_DISPOSABLE_SOCK.get())) {
            damageTravelSock(stack);
            setChanged();
            return;
        }

        if (SockDataUtil.getAthleticCognition(stack) > 0.0D && level.random.nextDouble() < ATHLETIC_COGNITION_LOSS_CHANCE) {
            SockDataUtil.setAthleticCognition(stack, SockDataUtil.getAthleticCognition(stack) - ATHLETIC_COGNITION_LOSS_AMOUNT);
            setChanged();
            return;
        }

        SockDataUtil.addDirtyCount(stack, 1);
        if (SockDataUtil.shouldBecomeDirty(stack)) {
            transformSockToDirty(stack);
        }
        setChanged();
    }

    private void damageTravelSock(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int multiplier = consumeDurabilityWithSockEnchantments(stack, serverLevel);
        if (multiplier <= 0) {
            return;
        }

        if (stack.isEmpty()) {
            extraMaxExtract = Math.max(0, extraMaxExtract + Config.TRAVEL_SOCK_MAX_EXTRACT_BOOST.get() * multiplier);
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    private void growCapacityWithBoatSock(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || SockDataUtil.isUnbreakable(stack)) {
            return;
        }

        int multiplier = consumeDurabilityWithSockEnchantments(stack, serverLevel);
        if (multiplier <= 0) {
            setChanged();
            return;
        }

        extraCapacity = Math.max(0, extraCapacity + SockDataUtil.getBoatSockCapacityBoost(stack) * multiplier);
        if (stack.isEmpty()) {
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
        setChanged();
    }

    private int consumeDurabilityWithSockEnchantments(ItemStack stack, ServerLevel serverLevel) {
        int humility = getSockEnchantmentLevel(stack, ModEnchantments.HUMILITY);
        if (humility > 0 && serverLevel.random.nextDouble() < Math.min(0.99D, 0.33D * Math.min(3, humility))) {
            return 0;
        }

        int multiplier = getPrideDurabilityMultiplier(stack);
        stack.hurtAndBreak(1, serverLevel, null, item -> {
        });
        return multiplier;
    }

    private int getPrideDurabilityMultiplier(ItemStack stack) {
        int pride = Math.min(3, getSockEnchantmentLevel(stack, ModEnchantments.PRIDE));
        int multiplier = 1;
        for (int i = 0; i < pride; i++) {
            multiplier *= 6;
        }
        return multiplier;
    }

    private int getSockEnchantmentLevel(ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        if (level == null) {
            return 0;
        }
        return stack.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment));
    }

    private void transformSockToDirty(ItemStack originalStack) {
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
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
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
        return slot == INPUT_SLOT && SockDataUtil.isGeneratorSock(stack);
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
        saveGeneratorData(tag, registries);
    }

    protected double getGenerationMultiplier() {
        return 1.0D;
    }

    protected double getDirtyChanceMultiplier() {
        return 1.0D;
    }

    protected boolean canConsumeGenerationResource(int precipitation) {
        return true;
    }

    protected void consumeGenerationResource(int precipitation) {
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
    }
}
