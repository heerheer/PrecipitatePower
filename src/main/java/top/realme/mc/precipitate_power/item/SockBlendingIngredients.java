package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class SockBlendingIngredients {
    private SockBlendingIngredients() {
    }

    public static TagKey<Item> tag(SockMaterial material) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(
                PrecipitatePower.MODID, "sock_blending/" + material.id()));
    }

    public static List<SockMaterial> findMaterials(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        return SockMaterial.VALUES.stream()
                .filter(material -> stack.is(tag(material)))
                .toList();
    }

    public static List<ItemStack> getIngredientStacks(SockMaterial material) {
        return BuiltInRegistries.ITEM.getTag(tag(material))
                .map(named -> named.stream()
                        .map(holder -> holder.value().getDefaultInstance())
                        .filter(stack -> !stack.isEmpty())
                        .toList())
                .orElseGet(List::of);
    }
}
