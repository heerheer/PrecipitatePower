package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> PRIDE = key("pride");
    public static final ResourceKey<Enchantment> HUMILITY = key("humility");

    private ModEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, name));
    }
}
