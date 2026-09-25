package top.realme.mc.precipitate_power.block.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import top.realme.mc.precipitate_power.item.SockBlendingIngredients;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.registry.ModAdvancements;
import top.realme.mc.precipitate_power.registry.ModBlockEntities;
import top.realme.mc.precipitate_power.registry.ModTags;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public final class SockBlenderBlockEntity extends BlockEntity implements Container {
    public static final int SOCK_SLOT = 0;
    public static final int INGREDIENT_SLOT = 1;
    public static final int MAX_MATERIALS = 5;
    private static final String SELECTED_PERCENT_TAG = "SelectedPercent";

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final InvWrapper itemHandler = new InvWrapper(this);
    private int selectedPercent = 10;
    private BlendStatus status = BlendStatus.IDLE;
    private SockMaterial statusMaterial;

    public SockBlenderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOCK_BLENDER.get(), pos, state);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public int getSelectedPercent() {
        return selectedPercent;
    }

    public void adjustSelectedPercent(int delta) {
        selectedPercent = Math.max(1, Math.min(50, selectedPercent + delta));
        status = BlendStatus.IDLE;
        statusMaterial = null;
        setChanged();
    }

    public Component getSelectedPercentText() {
        return Component.translatable("gui.precipitate_power.sock_blender.percentage", selectedPercent);
    }

    public Component getCurrentBlendText() {
        ItemStack sock = items.get(SOCK_SLOT);
        List<SockDataUtil.MaterialEntry> materials = SockDataUtil.getMaterials(sock);
        if (materials.isEmpty()) {
            return Component.translatable("gui.precipitate_power.sock_blender.no_materials");
        }
        MutableComponent result = Component.empty();
        for (int i = 0; i < materials.size(); i++) {
            SockDataUtil.MaterialEntry entry = materials.get(i);
            if (i > 0) {
                result.append("\n");
            }
            result.append(Component.translatable("material.precipitate_power." + entry.material().id()))
                    .append(Component.literal(String.format(java.util.Locale.ROOT, " %.1f%%", entry.share() * 100.0D)));
        }
        return result;
    }

    public Component getIngredientText() {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        List<SockMaterial> materials = SockBlendingIngredients.findMaterials(ingredient);
        if (ingredient.isEmpty()) {
            return Component.translatable("gui.precipitate_power.sock_blender.ingredient.empty");
        }
        if (materials.isEmpty()) {
            return Component.translatable("gui.precipitate_power.sock_blender.ingredient.unsupported");
        }
        if (materials.size() > 1) {
            return Component.translatable("gui.precipitate_power.sock_blender.ingredient.ambiguous");
        }
        return Component.translatable("gui.precipitate_power.sock_blender.ingredient.material",
                Component.translatable("material.precipitate_power." + materials.getFirst().id()));
    }

    public Component getStatusText() {
        return statusMaterial == null
                ? Component.translatable(status.translationKey)
                : Component.translatable(status.translationKey,
                        Component.translatable("material.precipitate_power." + statusMaterial.id()));
    }

    public void tryBlend(Player player) {
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack sock = items.get(SOCK_SLOT);
        if (sock.isEmpty()) {
            setStatus(BlendStatus.NO_SOCK, null);
            return;
        }
        if (!SockDataUtil.isBlendableSock(sock)) {
            setStatus(BlendStatus.SOCK_UNSUPPORTED, null);
            return;
        }

        List<SockDataUtil.MaterialEntry> current = SockDataUtil.getMaterials(sock).stream()
                .filter(entry -> entry.share() > 0.0D)
                .toList();
        double total = current.stream().mapToDouble(SockDataUtil.MaterialEntry::share).sum();
        if (current.size() >= MAX_MATERIALS) {
            setStatus(BlendStatus.FULL, null);
            return;
        }

        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        List<SockMaterial> candidates = SockBlendingIngredients.findMaterials(ingredient);
        if (ingredient.isEmpty()) {
            setStatus(BlendStatus.NO_INGREDIENT, null);
            return;
        }
        if (candidates.isEmpty()) {
            setStatus(BlendStatus.INGREDIENT_UNSUPPORTED, null);
            return;
        }
        if (candidates.size() > 1) {
            setStatus(BlendStatus.INGREDIENT_AMBIGUOUS, null);
            return;
        }

        SockMaterial newMaterial = candidates.getFirst();
        if (current.stream().anyMatch(entry -> entry.material() == newMaterial)) {
            setStatus(BlendStatus.DUPLICATE, newMaterial);
            return;
        }

        double newShare = selectedPercent / 100.0D;
        double oldScale = 1.0D - newShare;
        double normalization = total > 1.0D ? total : 1.0D;
        List<SockDataUtil.MaterialEntry> blended = new ArrayList<>(current.size() + 1);
        for (SockDataUtil.MaterialEntry entry : current) {
            blended.add(new SockDataUtil.MaterialEntry(
                    entry.material(), entry.share() / normalization * oldScale));
        }
        blended.add(new SockDataUtil.MaterialEntry(newMaterial, newShare));
        blended.sort(Comparator.comparing(entry -> entry.material().id()));

        SockDataUtil.setMaterials(sock, blended);
        SockDataUtil.recalculateDiamondDurability(sock);
        ingredient.shrink(1);
        if (ingredient.isEmpty()) {
            items.set(INGREDIENT_SLOT, ItemStack.EMPTY);
        }
        setStatus(BlendStatus.SUCCESS, newMaterial);
        if (blended.size() >= MAX_MATERIALS && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grant(serverPlayer, ModAdvancements.ULTIMATE_BLENDER);
        }
        level.playSound(null, worldPosition, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
    }

    private void setStatus(BlendStatus status, SockMaterial material) {
        this.status = status;
        this.statusMaterial = material;
        setChanged();
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
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
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
        status = BlendStatus.IDLE;
        statusMaterial = null;
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
        return slot == SOCK_SLOT
                ? stack.is(ModTags.Items.SOCK)
                : slot == INGREDIENT_SLOT && SockBlendingIngredients.findMaterials(stack).size() == 1;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        selectedPercent = Math.max(1, Math.min(50, tag.getInt(SELECTED_PERCENT_TAG)));
        if (!tag.contains(SELECTED_PERCENT_TAG)) {
            selectedPercent = 10;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt(SELECTED_PERCENT_TAG, selectedPercent);
    }

    private enum BlendStatus {
        IDLE("gui.precipitate_power.sock_blender.status.idle"),
        NO_SOCK("gui.precipitate_power.sock_blender.status.no_sock"),
        SOCK_UNSUPPORTED("gui.precipitate_power.sock_blender.status.sock_unsupported"),
        FULL("gui.precipitate_power.sock_blender.status.full"),
        NO_INGREDIENT("gui.precipitate_power.sock_blender.status.no_ingredient"),
        INGREDIENT_UNSUPPORTED("gui.precipitate_power.sock_blender.status.ingredient_unsupported"),
        INGREDIENT_AMBIGUOUS("gui.precipitate_power.sock_blender.status.ingredient_ambiguous"),
        DUPLICATE("gui.precipitate_power.sock_blender.status.duplicate"),
        SUCCESS("gui.precipitate_power.sock_blender.status.success");

        private final String translationKey;

        BlendStatus(String translationKey) {
            this.translationKey = translationKey;
        }
    }
}
