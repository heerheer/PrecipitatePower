package top.realme.mc.precipitate_power.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.item.ElectricSockItem;
import top.realme.mc.precipitate_power.registry.ModDataComponents;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.registry.ModRecipes;

public final class ElectricSockUpgradeRecipe extends CustomRecipe {
    private final Upgrade upgrade;

    public ElectricSockUpgradeRecipe(CraftingBookCategory category, Upgrade upgrade) {
        super(category);
        this.upgrade = upgrade;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        ItemStack source = input.getItem(1, 1);
        if (!source.is(upgrade.source()) || !ElectricSockItem.isInventoryEmpty(source)) {
            return false;
        }
        return upgrade == Upgrade.MEDIUM ? matchesMedium(input) : matchesLarge(input);
    }

    private static boolean matchesMedium(CraftingInput input) {
        return input.getItem(0, 0).is(Items.GOLD_BLOCK)
                && input.getItem(1, 0).is(Items.STRING)
                && input.getItem(2, 0).is(Items.GOLD_BLOCK)
                && input.getItem(0, 1).is(ItemTags.WOOL)
                && input.getItem(2, 1).is(ItemTags.WOOL)
                && input.getItem(0, 2).is(Items.COPPER_BLOCK)
                && input.getItem(1, 2).is(Items.STRING)
                && input.getItem(2, 2).is(Items.COPPER_BLOCK);
    }

    private static boolean matchesLarge(CraftingInput input) {
        return input.getItem(0, 0).is(Items.GOLD_BLOCK)
                && input.getItem(1, 0).is(ItemTags.WOOL)
                && input.getItem(2, 0).is(Items.GOLD_BLOCK)
                && input.getItem(0, 1).is(Items.COPPER_BLOCK)
                && input.getItem(2, 1).is(Items.COPPER_BLOCK)
                && input.getItem(0, 2).is(Items.STRING)
                && input.getItem(1, 2).is(Items.GOLD_BLOCK)
                && input.getItem(2, 2).is(Items.STRING);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack source = input.getItem(1, 1);
        ItemStack result = source.transmuteCopy(upgrade.target(), 1);
        ElectricSockItem sourceItem = (ElectricSockItem) source.getItem();
        ElectricSockItem targetItem = (ElectricSockItem) result.getItem();
        int preservedEnergy = Math.min(
                sourceItem.createEnergyStorage(source).getEnergyStored(), targetItem.getCapacity());
        result.set(ModDataComponents.ELECTRIC_SOCK_ENERGY.get(), preservedEnergy);
        ElectricSockItem.updateAppearance(result);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(upgrade.target());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return upgrade == Upgrade.MEDIUM
                ? ModRecipes.SMALL_TO_MEDIUM_ELECTRIC_SOCK.get()
                : ModRecipes.MEDIUM_TO_LARGE_ELECTRIC_SOCK.get();
    }

    public enum Upgrade {
        MEDIUM,
        LARGE;

        public Item source() {
            return this == MEDIUM ? ModItems.SMALL_ELECTRIC_SOCK.get() : ModItems.MEDIUM_ELECTRIC_SOCK.get();
        }

        public Item target() {
            return this == MEDIUM ? ModItems.MEDIUM_ELECTRIC_SOCK.get() : ModItems.LARGE_ELECTRIC_SOCK.get();
        }
    }
}
