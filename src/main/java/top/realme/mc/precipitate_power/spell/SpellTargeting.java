package top.realme.mc.precipitate_power.spell;

import io.redspace.ironsspellbooks.api.util.RaycastBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import top.realme.mc.precipitate_power.entity.SockOfferingAltarEntity;

final class SpellTargeting {
    private SpellTargeting() {
    }

    static LivingEntity findTarget(Level level, LivingEntity caster, float range, boolean enemiesOnly) {
        HitResult hitResult = RaycastBuilder.begin(level, caster)
                .range(range)
                .checkForBlocks(true)
                .bbInflation(0.45F)
                .filter(entity -> entity instanceof LivingEntity living
                        && living != caster
                        && living.isAlive()
                        && !living.isSpectator()
                        && (!enemiesOnly || !caster.isAlliedTo(living)))
                .build();
        return hitResult instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity living ? living : null;
    }

    static Entity findBarrageTarget(Level level, LivingEntity caster, float range) {
        HitResult hitResult = RaycastBuilder.begin(level, caster)
                .range(range)
                .checkForBlocks(true)
                .bbInflation(0.45F)
                .filter(entity -> entity instanceof SockOfferingAltarEntity
                        || entity instanceof LivingEntity living
                        && living != caster
                        && living.isAlive()
                        && !living.isSpectator()
                        && !caster.isAlliedTo(living))
                .build();
        return hitResult instanceof EntityHitResult entityHitResult
                ? entityHitResult.getEntity() : null;
    }
}
