package top.realme.mc.precipitate_power.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModEffects;

import java.util.List;
import java.util.Optional;

public class SockWardSpell extends AbstractSpell {
    private static final int MAX_SOCK_COUNT = 6;
    private static final int[] SOCK_COUNTS = {2, 3, 4, 5, 6, 6};
    private static final int[] DAMAGE_REDUCTION_PERCENT = {10, 12, 15, 20, 30, 30};
    private static final int[] COOLDOWN_SECONDS = {120, 120, 100, 100, 90, 90};
    private static final float TARGET_RANGE = 24.0F;

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "sock_ward");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(COOLDOWN_SECONDS[0])
            .build();

    public SockWardSpell() {
        baseManaCost = 80;
        manaCostPerLevel = 0;
        baseSpellPower = 12;
        spellPowerPerLevel = 3;
        castTime = 20;
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
        return CastType.LONG;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_TWO_HANDS;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_CAST_SPELL);
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SockTargetCastData();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        LivingEntity aimedTarget = SpellTargeting.findTarget(level, caster, TARGET_RANGE, false);
        LivingEntity target = aimedTarget instanceof Player ? aimedTarget : caster;
        magicData.setAdditionalCastData(new SockTargetCastData(target));
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel
                && magicData.getAdditionalCastData() instanceof SockTargetCastData castData) {
            LivingEntity target = castData.getTarget(serverLevel);
            if (target != null) {
                target.addEffect(new MobEffectInstance(
                        ModEffects.SOCK_WARD,
                        getDuration(spellLevel, caster),
                        encodeEffectAmplifier(spellLevel, getSockCount(spellLevel)),
                        false,
                        true,
                        true));
            }
        }
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return Math.max(20, Math.round(getSpellPower(spellLevel, caster) * 20.0F));
    }

    @Override
    public int getManaCost(int spellLevel) {
        int configuredBaseCost = super.getManaCost(1);
        return spellLevel >= 5 ? Math.round(configuredBaseCost * 1.25F) : configuredBaseCost;
    }

    public static int getSockCount(int spellLevel) {
        return SOCK_COUNTS[levelIndex(spellLevel)];
    }

    public static int getDamageReductionPercent(int spellLevel) {
        return DAMAGE_REDUCTION_PERCENT[levelIndex(spellLevel)];
    }

    public static int getBaseCooldownSeconds(int spellLevel) {
        return COOLDOWN_SECONDS[levelIndex(spellLevel)];
    }

    public static int encodeEffectAmplifier(int spellLevel, int remainingSocks) {
        int clampedLevel = Math.max(1, Math.min(SOCK_COUNTS.length, spellLevel));
        int clampedSocks = Math.max(1, Math.min(MAX_SOCK_COUNT, remainingSocks));
        return (clampedLevel - 1) * MAX_SOCK_COUNT + clampedSocks - 1;
    }

    public static int getEffectSpellLevel(int amplifier) {
        return Math.max(1, Math.min(SOCK_COUNTS.length, amplifier / MAX_SOCK_COUNT + 1));
    }

    public static int getRemainingSockCount(int amplifier) {
        return Math.max(1, Math.min(MAX_SOCK_COUNT, amplifier % MAX_SOCK_COUNT + 1));
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.precipitate_power.effect_duration",
                        String.format("%.1f", getDuration(spellLevel, caster) / 20.0F)),
                Component.translatable("ui.precipitate_power.sock_count", getSockCount(spellLevel)),
                Component.translatable("ui.precipitate_power.damage_reduction",
                        getDamageReductionPercent(spellLevel)));
    }

    private static int levelIndex(int spellLevel) {
        return Math.max(0, Math.min(SOCK_COUNTS.length - 1, spellLevel - 1));
    }
}
