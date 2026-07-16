package top.realme.mc.precipitate_power.item;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ComponentItemHandler;
import top.realme.mc.precipitate_power.menu.ElectricSockMenu;
import top.realme.mc.precipitate_power.registry.ModDataComponents;

public class ElectricSockItem extends Item implements HeldItemUIMenuType.HeldItemUI {
    public static final int INTERNAL_SLOTS = 2;

    private final IntSupplier capacity;
    private final IntSupplier transferRate;

    public ElectricSockItem(Properties properties, IntSupplier capacity, IntSupplier transferRate) {
        super(properties);
        this.capacity = capacity;
        this.transferRate = transferRate;
    }

    public int getCapacity() {
        return Math.max(0, capacity.getAsInt());
    }

    public int getTransferRate() {
        return Math.max(0, transferRate.getAsInt());
    }

    public IEnergyStorage createEnergyStorage(ItemStack stack) {
        return new ElectricSockEnergyStorage(stack, getCapacity(), getTransferRate());
    }

    public ComponentItemHandler createItemHandler(ItemStack stack) {
        return new ComponentItemHandler(stack, ModDataComponents.ELECTRIC_SOCK_INVENTORY.get(), INTERNAL_SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack candidate) {
                return super.isItemValid(slot, candidate) && !(candidate.getItem() instanceof ElectricSockItem);
            }

            @Override
            protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack) {
                updateAppearance(stack);
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            HeldItemUIMenuType.openUI(serverPlayer, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        return ElectricSockMenu.createUI(holder.itemStack, holder.player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        IEnergyStorage storage = createEnergyStorage(stack);
        tooltipComponents.add(Component.translatable("tooltip.precipitate_power.electric_sock.energy",
                storage.getEnergyStored(), storage.getMaxEnergyStored()).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.precipitate_power.electric_sock.transfer",
                getTransferRate()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.precipitate_power.electric_sock.open")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static boolean isInventoryEmpty(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ELECTRIC_SOCK_INVENTORY.get(), ItemContainerContents.EMPTY)
                .nonEmptyStream().findAny().isEmpty();
    }

    public static void updateAppearance(ItemStack stack) {
        ItemContainerContents contents = stack.getOrDefault(
                ModDataComponents.ELECTRIC_SOCK_INVENTORY.get(), ItemContainerContents.EMPTY);
        int occupied = (int) contents.nonEmptyStream().limit(INTERNAL_SLOTS).count();
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(occupied));
    }
}
