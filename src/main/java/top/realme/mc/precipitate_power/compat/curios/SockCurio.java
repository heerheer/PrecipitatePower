package top.realme.mc.precipitate_power.compat.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.realme.mc.precipitate_power.PrecipitatePower;
import net.neoforged.fml.ModList;
import top.realme.mc.precipitate_power.compat.ironsspellbooks.IronsSpellbooksCompat;
import top.realme.mc.precipitate_power.registry.ModItems;
import top.realme.mc.precipitate_power.util.SockDataUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class SockCurio implements ICurioItem {
    public static final SockCurio INSTANCE = new SockCurio();
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "sock_speed");
    private static final ResourceLocation DIRTY_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "dirty_sock_speed");
    private static final ResourceLocation BOAT_PAIR_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "boat_sock_pair_speed");
    private static final ResourceLocation RAINBOW_DAMAGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "rainbow_sock_attack_damage");
    private static final ResourceLocation RAINBOW_ARMOR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "rainbow_sock_armor");
    private static final ResourceLocation RAINBOW_HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "rainbow_sock_max_health");
    private static final ResourceLocation RAINBOW_ATTACK_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "rainbow_sock_attack_speed");

    private SockCurio() {
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            ResourceLocation id,
            ItemStack stack
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();

        if (stack.is(ModItems.DIRTY_WHITE_SOCK.get())) {
            addMultiplierModifier(modifiers, Attributes.MOVEMENT_SPEED, DIRTY_SPEED_MODIFIER_ID, SockDataUtil.DIRTY_SOCK_SPEED_PENALTY);
            return modifiers;
        }

        if (stack.is(ModItems.BOAT_SOCK.get())) {
            addMultiplierModifier(modifiers, Attributes.MOVEMENT_SPEED, id.withSuffix("_boat_speed"), SockDataUtil.BOAT_SOCK_SPEED_PENALTY);
            if (isFirstBoatSock(slotContext) && countEquippedBoatSocks(slotContext) >= 2) {
                addMultiplierModifier(modifiers, Attributes.MOVEMENT_SPEED, BOAT_PAIR_SPEED_MODIFIER_ID, SockDataUtil.BOAT_SOCK_PAIR_SPEED_PENALTY);
            }
            return modifiers;
        }

        double athleticCognition = SockDataUtil.getAthleticCognition(stack);
        if (athleticCognition > 0.0D) {
            addMultiplierModifier(modifiers, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, athleticCognition * 0.45D);
        }

        if (stack.is(ModItems.RAINBOW_WHITE_SOCK.get())) {
            addValueModifier(modifiers, Attributes.ATTACK_DAMAGE, RAINBOW_DAMAGE_MODIFIER_ID, 2.0D);
            addValueModifier(modifiers, Attributes.ARMOR, RAINBOW_ARMOR_MODIFIER_ID, 7.0D);
            addValueModifier(modifiers, Attributes.MAX_HEALTH, RAINBOW_HEALTH_MODIFIER_ID, 7.0D);
            if (ModList.get().isLoaded("irons_spellbooks")) {
                IronsSpellbooksCompat.addRainbowSockModifiers(modifiers);
            } else {
                addMultiplierModifier(modifiers, Attributes.ATTACK_SPEED, RAINBOW_ATTACK_SPEED_MODIFIER_ID, 0.08D);
            }
        }
        return modifiers;
    }

    private static int countEquippedBoatSocks(SlotContext slotContext) {
        if (slotContext.entity() == null) {
            return 0;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .map(handler -> handler.findCurios(result -> result.is(ModItems.BOAT_SOCK.get())).size())
                .orElse(0);
    }

    private static boolean isFirstBoatSock(SlotContext slotContext) {
        if (slotContext.entity() == null) {
            return false;
        }
        return CuriosApi.getCuriosInventory(slotContext.entity())
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.is(ModItems.BOAT_SOCK.get())))
                .map(result -> result.slotContext().identifier().equals(slotContext.identifier())
                        && result.slotContext().index() == slotContext.index())
                .orElse(false);
    }

    private static void addMultiplierModifier(
            Multimap<Holder<Attribute>, AttributeModifier> modifiers,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount
    ) {
        modifiers.put(
                attribute,
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        );
    }

    private static void addValueModifier(
            Multimap<Holder<Attribute>, AttributeModifier> modifiers,
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount
    ) {
        modifiers.put(
                attribute,
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE)
        );
    }
}
