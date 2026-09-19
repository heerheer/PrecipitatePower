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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.GiantSockProjectileEntity;

import java.util.List;
import java.util.Optional;

public class GiantSockSpell extends AbstractSpell {
    private static final float[] BASE_DAMAGE = {4.0F, 4.5F, 5.5F, 7.0F, 9.0F};
    private static final float[] COLLISION_SCALE = {1.0F, 1.1F, 1.2F, 1.3F, 1.5F};

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "giant_sock");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(8.0D)
            .build();

    public GiantSockSpell() {
        baseManaCost = 40;
        manaCostPerLevel = 0;
        baseSpellPower = 4;
        spellPowerPerLevel = 0;
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
        return Optional.of(SoundEvents.SNOWBALL_THROW);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData magicData) {
        if (!level.isClientSide()) {
            Vec3 look = caster.getLookAngle().normalize();
            GiantSockProjectileEntity projectile =
                    new GiantSockProjectileEntity(
                            level, caster, getDamage(spellLevel, caster), getCollisionScale(spellLevel));
            projectile.setPos(caster.getEyePosition().add(look.scale(1.2D)));
            projectile.shoot(look, 0.38D);
            level.addFreshEntity(projectile);
        }
        super.onCast(level, spellLevel, caster, castSource, magicData);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.precipitate_power.damage",
                String.format("%.1f", getDamage(spellLevel, caster))));
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return BASE_DAMAGE[levelIndex(spellLevel)] * getEntityPowerMultiplier(caster);
    }

    private float getCollisionScale(int spellLevel) {
        return COLLISION_SCALE[levelIndex(spellLevel)];
    }

    private int levelIndex(int spellLevel) {
        return Math.max(0, Math.min(BASE_DAMAGE.length - 1, spellLevel - 1));
    }
}
