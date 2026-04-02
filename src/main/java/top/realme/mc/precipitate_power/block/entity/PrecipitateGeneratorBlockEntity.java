package top.realme.mc.precipitate_power.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.joml.Math;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.menu.PrecipitateGeneratorMenu;
import top.realme.mc.precipitate_power.registry.ModBlockEntities;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.FormulaParser;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class PrecipitateGeneratorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private static final int[] TOP_SLOTS = new int[]{INPUT_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{OUTPUT_SLOT};
    private static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage();
    private final InvWrapper internalItemHandler = new InvWrapper(this);
    private final SidedInvWrapper upwardItemHandler = new SidedInvWrapper(this, Direction.UP);
    private final SidedInvWrapper downwardItemHandler = new SidedInvWrapper(this, Direction.DOWN);

    // 定义container的数据接口，用于GUI显示和同步
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> SockDataUtil.getPrecipitationLevel(items.get(INPUT_SLOT));
                case 3 -> SockDataUtil.getDirtyCount(items.get(INPUT_SLOT));
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
            return 4;
        }
    };

    public PrecipitateGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PRECIPITATE_GENERATOR.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrecipitateGeneratorBlockEntity blockEntity) {
        blockEntity.tickServer();
    }

    private void tickServer() {
        ItemStack stack = items.get(INPUT_SLOT);
        if (!SockDataUtil.isGeneratorSock(stack)) {
            return;
        }

        // 获取沉淀等级
        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        // 获取产能倍率
        double coefficient = SockDataUtil.getPowerCoefficient(stack);
        // 传入等级计算基础发电量
        double baseGeneration = FormulaParser.evaluate(Config.GENERATION_FORMULA.get(), precipitation);
        // 计算最终发电量，向下取整并确保不为负数
        int generated = (int) Math.max(0, Math.floor(baseGeneration * coefficient));


        if (generated > 0) {
            energyStorage.addGeneratedEnergy(generated);
            //level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (level.getGameTime() % 20L == 0L && level.random.nextDouble() < Config.PRECIPITATE_CHANCE.get()) {
            SockDataUtil.addPrecipitation(stack, 1);
            setChanged();
        }

        if (!SockDataUtil.isUnbreakable(stack)) {
            double dirtyChance = Config.DIRTY_BASE_CHANCE.get() + SockDataUtil.getPrecipitationLevel(stack) * Config.DIRTY_CHANCE_PER_PRECIPITATION.get();
            int unbreaking = stack.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING));
            dirtyChance /= (1.0D + unbreaking);
            if (level.random.nextDouble() < dirtyChance) {
                SockDataUtil.addDirtyCount(stack, 1);
                if (SockDataUtil.shouldBecomeDirty(stack)) {
                    transformSockToDirty(stack);
                }
                setChanged();
            }
        }
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

    public EnergyStorage getEnergyStorage() {
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

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.precipitate_power.precipitate_generator");
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
        energyStorage.setEnergy(tag.getInt("Energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    private final class GeneratorEnergyStorage extends EnergyStorage {
        private GeneratorEnergyStorage() {
            super(Config.GENERATOR_CAPACITY.get(), 0, Integer.MAX_VALUE);
        }

        private void setEnergy(int energy) {
            this.energy = Math.max(0, Math.min(getMaxEnergyStored(), energy));
        }


        private int addGeneratedEnergy(int amount) {
            if (amount <= 0) return 0;
            int accepted = Math.min(getMaxEnergyStored() - this.energy, amount);
            this.energy += accepted;
            return accepted;
        }

    }
}
