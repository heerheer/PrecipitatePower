package top.realme.mc.precipitate_power.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.BloodiPowerAreaEntity;

import java.util.List;
import java.util.Optional;

public class BloodiPowerSpell extends AbstractSpell {
    private static final int[] DURATION_SECONDS = {6, 8, 10, 12, 15};
    private static final int[] COOLDOWN_SECONDS = {75, 70, 60, 60, 55};

    public static final float DAMAGE_IMMUNITY_THRESHOLD = 0.5F;
    public static final int BARRAGE_PROC_CHANCE_PERCENT = 35;
    public static final int MOVEMENT_SPEED_PERCENT = 20;

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "bloodi_power");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(COOLDOWN_SECONDS[0])
            .build();

    public BloodiPowerSpell() {
        baseManaCost = 60;
        manaCostPerLevel = 0;
        baseSpellPower = 8;
        spellPowerPerLevel = 2;
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
        return Optional.of(SoundEvents.BEACON_ACTIVATE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster,
                       CastSource castSource, MagicData magicData) {
        if (!level.isClientSide()) {
            BloodiPowerAreaEntity area = new BloodiPowerAreaEntity(
                    level, caster, getRadius(spellLevel), getDuration(spellLevel, caster));
            level.addFreshEntity(area);
        }
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }

    public float getRadius(int spellLevel) {
        return 4.0F + Math.max(0, spellLevel - 1) * 0.25F;
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return DURATION_SECONDS[levelIndex(spellLevel)] * 20;
    }

    public static int getBaseCooldownSeconds(int spellLevel) {
        return COOLDOWN_SECONDS[levelIndex(spellLevel)];
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.precipitate_power.effect_duration",
                        String.format("%.1f", getDuration(spellLevel, caster) / 20.0F)),
                Component.translatable("ui.precipitate_power.effect_radius",
                        String.format("%.1f", getRadius(spellLevel))),
                Component.translatable("ui.precipitate_power.movement_speed_bonus", MOVEMENT_SPEED_PERCENT),
                Component.translatable("ui.precipitate_power.damage_immunity_threshold",
                        String.format("%.1f", DAMAGE_IMMUNITY_THRESHOLD)),
                Component.translatable("ui.precipitate_power.barrage_proc_chance", BARRAGE_PROC_CHANCE_PERCENT));
    }

    private static int levelIndex(int spellLevel) {
        return Math.max(0, Math.min(DURATION_SECONDS.length - 1, spellLevel - 1));
    }
}
