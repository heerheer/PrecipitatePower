package top.realme.mc.precipitate_power.compat.ironsspellbooks;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class IronsSpellbooksCompat {
    private static final ResourceLocation RAINBOW_COOLDOWN_REDUCTION_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "rainbow_sock_cooldown_reduction");

    private IronsSpellbooksCompat() {
    }

    public static void addRainbowSockModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers) {
        modifiers.put(
                AttributeRegistry.COOLDOWN_REDUCTION,
                new AttributeModifier(RAINBOW_COOLDOWN_REDUCTION_MODIFIER_ID, 0.08D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        );
    }
}
