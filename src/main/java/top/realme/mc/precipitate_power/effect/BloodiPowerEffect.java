package top.realme.mc.precipitate_power.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import top.realme.mc.precipitate_power.PrecipitatePower;

public class BloodiPowerEffect extends MobEffect {
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "bloodi_power_speed");

    public BloodiPowerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x13A8E8);
        addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID,
                0.20D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
