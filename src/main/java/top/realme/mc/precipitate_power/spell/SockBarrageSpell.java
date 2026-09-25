package top.realme.mc.precipitate_power.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.registry.ModEffects;

import java.util.List;
import java.util.Optional;

public class SockBarrageSpell extends AbstractSpell {
    private static final float[] BASE_DAMAGE = {2.5F, 2.5F, 3.0F, 3.5F, 4.0F};
    private static final int[] PROJECTILE_COUNTS = {3, 3, 4, 6, 8};
    private static final float TARGET_RANGE = 24.0F;

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "sock_barrage");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20.0D)
            .build();

    public SockBarrageSpell() {
        baseManaCost = 80;
        manaCostPerLevel = 0;
        baseSpellPower = 5;
        spellPowerPerLevel = 2;
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
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        return SpellTargeting.findBarrageTarget(level, caster, TARGET_RANGE) != null;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            Entity target = SpellTargeting.findBarrageTarget(level, caster, TARGET_RANGE);
            if (target != null) {
                float damage = getDamage(spellLevel, caster);
                float bonusTrueDamage = 0.0F;
                if (target instanceof LivingEntity
                        && caster.hasEffect(ModEffects.BLOODI_POWER)
                        && caster.getRandom().nextInt(100) < BloodiPowerSpell.BARRAGE_PROC_CHANCE_PERCENT) {
                    bonusTrueDamage = 1.0F + getSpellPower(spellLevel, caster) * 0.18F;
                }
                SockBarrageManager.start(
                        serverLevel, caster, target, getProjectileCount(spellLevel), damage, bonusTrueDamage);
            }
        }
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.precipitate_power.damage_per_sock",
                        String.format("%.1f", getDamage(spellLevel, caster))),
                Component.translatable("ui.precipitate_power.sock_count", getProjectileCount(spellLevel)));
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return BASE_DAMAGE[levelIndex(spellLevel)] * getEntityPowerMultiplier(caster);
    }

    private int getProjectileCount(int spellLevel) {
        return PROJECTILE_COUNTS[levelIndex(spellLevel)];
    }

    private int levelIndex(int spellLevel) {
        return Math.max(0, Math.min(BASE_DAMAGE.length - 1, spellLevel - 1));
    }
}
