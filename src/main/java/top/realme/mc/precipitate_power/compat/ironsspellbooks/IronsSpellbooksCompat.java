package top.realme.mc.precipitate_power.compat.ironsspellbooks;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.item.SockMaterial;
import top.realme.mc.precipitate_power.util.SockDataUtil;

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

    public static void addMaterialModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers, ResourceLocation id, ItemStack stack) {
        addValueModifier(modifiers, AttributeRegistry.MAX_MANA, id.withSuffix("_mithril_weave_max_mana"),
                SockDataUtil.getMaterialScalar(stack, SockMaterial.MITHRIL_WEAVE, SockMaterial::maxManaBonus));
        addMultiplierModifier(modifiers, AttributeRegistry.SPELL_POWER, id.withSuffix("_mithril_weave_spell_power"),
                SockDataUtil.getMaterialScalar(stack, SockMaterial.MITHRIL_WEAVE, SockMaterial::spellPowerBonus));
        addMultiplierModifier(modifiers, AttributeRegistry.COOLDOWN_REDUCTION, id.withSuffix("_arcane_cloth_cooldown_reduction"),
                SockDataUtil.getMaterialScalar(stack, SockMaterial.ARCANE_CLOTH, SockMaterial::cooldownReductionBonus));
        addMultiplierModifier(modifiers, AttributeRegistry.CAST_TIME_REDUCTION, id.withSuffix("_arcane_cloth_cast_time_reduction"),
                SockDataUtil.getMaterialScalar(stack, SockMaterial.ARCANE_CLOTH, SockMaterial::castTimeReductionBonus));
        addMultiplierModifier(modifiers, AttributeRegistry.MANA_REGEN, id.withSuffix("_arcane_cloth_mana_regen"),
                SockDataUtil.getMaterialScalar(stack, SockMaterial.ARCANE_CLOTH, SockMaterial::manaRegenBonus));
    }

    private static void addMultiplierModifier(
            Multimap<Holder<Attribute>, AttributeModifier> modifiers,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount
    ) {
        if (amount > 0.0D) {
            modifiers.put(attribute, new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private static void addValueModifier(
            Multimap<Holder<Attribute>, AttributeModifier> modifiers,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount
    ) {
        if (amount > 0.0D) {
            modifiers.put(attribute, new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
