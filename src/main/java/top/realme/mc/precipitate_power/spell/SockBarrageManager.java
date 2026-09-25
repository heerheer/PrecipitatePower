package top.realme.mc.precipitate_power.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.entity.BloodiProjectileEntity;
import top.realme.mc.precipitate_power.entity.HomingSockProjectileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = PrecipitatePower.MODID)
public final class SockBarrageManager {
    private static final int SHOT_INTERVAL_TICKS = 10;
    private static final Map<ServerLevel, List<BarrageSequence>> ACTIVE_BARRAGES = new WeakHashMap<>();

    private SockBarrageManager() {
    }

    public static void start(ServerLevel level, LivingEntity caster, Entity target,
                             int projectileCount, float damage, float bonusTrueDamage) {
        int bonusShotIndex = bonusTrueDamage > 0.0F ? level.random.nextInt(projectileCount) : -1;
        BarrageSequence sequence = new BarrageSequence(
                caster.getUUID(), target.getUUID(), projectileCount, damage,
                bonusTrueDamage, bonusShotIndex, level.getGameTime());
        if (spawnNextShot(level, sequence) && sequence.hasRemainingShots()) {
            sequence.nextShotTick = level.getGameTime() + SHOT_INTERVAL_TICKS;
            ACTIVE_BARRAGES.computeIfAbsent(level, ignored -> new ArrayList<>()).add(sequence);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        List<BarrageSequence> sequences = ACTIVE_BARRAGES.get(level);
        if (sequences == null || sequences.isEmpty()) {
            return;
        }

        long gameTime = level.getGameTime();
        sequences.removeIf(sequence -> {
            if (gameTime < sequence.nextShotTick) {
                return false;
            }
            if (!spawnNextShot(level, sequence) || !sequence.hasRemainingShots()) {
                return true;
            }
            sequence.nextShotTick = gameTime + SHOT_INTERVAL_TICKS;
            return false;
        });
        if (sequences.isEmpty()) {
            ACTIVE_BARRAGES.remove(level);
        }
    }

    private static boolean spawnNextShot(ServerLevel level, BarrageSequence sequence) {
        Entity casterEntity = level.getEntity(sequence.casterUuid);
        Entity targetEntity = level.getEntity(sequence.targetUuid);
        if (!(casterEntity instanceof LivingEntity caster) || !caster.isAlive()
                || targetEntity == null || !targetEntity.isAlive()) {
            return false;
        }

        Vec3 spawnPosition = caster.getEyePosition().add(caster.getLookAngle().scale(0.45D));
        HomingSockProjectileEntity projectile =
                new HomingSockProjectileEntity(level, caster, targetEntity, sequence.damage);
        projectile.setPos(spawnPosition);
        level.addFreshEntity(projectile);

        if (sequence.shotsFired == sequence.bonusShotIndex
                && targetEntity instanceof LivingEntity target) {
            BloodiProjectileEntity bonusProjectile =
                    new BloodiProjectileEntity(level, caster, target, sequence.bonusTrueDamage);
            bonusProjectile.setPos(spawnPosition);
            level.addFreshEntity(bonusProjectile);
        }
        sequence.shotsFired++;
        return true;
    }

    private static final class BarrageSequence {
        private final UUID casterUuid;
        private final UUID targetUuid;
        private final int projectileCount;
        private final float damage;
        private final float bonusTrueDamage;
        private final int bonusShotIndex;
        private int shotsFired;
        private long nextShotTick;

        private BarrageSequence(UUID casterUuid, UUID targetUuid, int projectileCount,
                                float damage, float bonusTrueDamage, int bonusShotIndex,
                                long nextShotTick) {
            this.casterUuid = casterUuid;
            this.targetUuid = targetUuid;
            this.projectileCount = projectileCount;
            this.damage = damage;
            this.bonusTrueDamage = bonusTrueDamage;
            this.bonusShotIndex = bonusShotIndex;
            this.nextShotTick = nextShotTick;
        }

        private boolean hasRemainingShots() {
            return shotsFired < projectileCount;
        }
    }
}
