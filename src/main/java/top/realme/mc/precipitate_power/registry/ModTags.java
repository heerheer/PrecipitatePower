package top.realme.mc.precipitate_power.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    private ModTags() {
    }

    public static final class Items {
        public static final TagKey<Item> SOCK = TagKey.create(
                Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "sock"));

        private Items() {
        }
    }
}
