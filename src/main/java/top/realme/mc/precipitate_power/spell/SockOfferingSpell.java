package top.realme.mc.precipitate_power.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.SockOfferingAltarEntity;
import top.realme.mc.precipitate_power.registry.ModSounds;

import java.util.List;
import java.util.Optional;

public class SockOfferingSpell extends AbstractSpell {
    private static final float TARGET_RANGE = 16.0F;

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "sock_offering");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(180.0D)
            .build();

    public SockOfferingSpell() {
        baseManaCost = 120;
        manaCostPerLevel = 0;
        baseSpellPower = 5;
        spellPowerPerLevel = 1;
        castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(ModSounds.SOCK_OFFERING.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster,
                       CastSource castSource, MagicData magicData) {
        if (!level.isClientSide()) {
            Vec3 placement = findPlacement(level, caster);
            SockOfferingAltarEntity altar = new SockOfferingAltarEntity(
                    level, caster, placement, spellLevel, getEntityPowerMultiplier(caster));
            level.addFreshEntity(altar);
        }
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }

    private static Vec3 findPlacement(Level level, LivingEntity caster) {
        HitResult aimed = RaycastBuilder.begin(level, caster)
                .range(TARGET_RANGE)
                .checkForBlocks(true)
                .bbInflation(0.35F)
                .filter(entity -> entity != caster && entity.isAlive())
                .build();
        Vec3 desired = aimed.getType() == HitResult.Type.MISS
                ? caster.getEyePosition().add(caster.getLookAngle().scale(8.0D))
                : aimed.getLocation();
        BlockHitResult ground = level.clip(new ClipContext(
                desired.add(0.0D, 2.0D, 0.0D),
                desired.add(0.0D, -6.0D, 0.0D),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster));
        if (ground.getType() != HitResult.Type.MISS) {
            Vec3 location = ground.getLocation();
            return new Vec3(location.x, location.y + 0.02D, location.z);
        }
        return desired;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        int quality = Math.max(1, Math.min(5, spellLevel));
        return List.of(
                Component.translatable("ui.precipitate_power.offering_duration", 15),
                Component.translatable("ui.precipitate_power.offering_radius",
                        String.format("%.1f", SockOfferingAltarEntity.getAttackRadius(quality))),
                Component.translatable("ui.precipitate_power.offering_milestone_bonus"),
                Component.translatable("ui.precipitate_power.offering_level_cap",
                        SockOfferingAltarEntity.MAX_OFFERING_LEVEL),
                Component.translatable("ui.precipitate_power.offering_initial_level", spellLevel),
                Component.translatable("ui.precipitate_power.offering_attack_interval",
                        String.format("%.1f", SockOfferingAltarEntity.getBasicAttackInterval(quality) / 20.0F)),
                Component.translatable("ui.precipitate_power.offering_basic_damage",
                        String.format("%.2f", SockOfferingAltarEntity.getBasicProjectileDamage(
                                quality, getEntityPowerMultiplier(caster)))),
                Component.translatable("ui.precipitate_power.offering_reactive_damage",
                        String.format("%.1f", SockOfferingAltarEntity.getReactiveProjectileDamage(quality))));
    }
}
