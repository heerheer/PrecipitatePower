package top.realme.mc.precipitate_power.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.realme.mc.precipitate_power.registry.ModEntities;
import top.realme.mc.precipitate_power.registry.ModSounds;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class SockOfferingAltarEntity extends Entity {
    public static final int MAX_OFFERING_LEVEL = 100;
    public static final int BASE_DURATION_TICKS = 300;
    public static final float MAX_ATTACK_RADIUS = 12.0F;

    private static final float BASE_PROJECTILE_DAMAGE = 2.5F;
    private static final float[] QUALITY_DAMAGE_BONUS = {0.5F, 0.75F, 1.0F, 1.5F, 2.0F};
    private static final float[] REACTIVE_DAMAGE = {4.0F, 5.0F, 6.0F, 7.0F, 8.0F};
    private static final int[] BASIC_ATTACK_INTERVAL = {60, 60, 60, 40, 40};
    private static final float[] ATTACK_RADIUS = {5.0F, 6.0F, 7.0F, 8.0F, 9.0F};
    private static final int REACTIVE_COOLDOWN_TICKS = 20;
    private static final int MILESTONE_SHOT_INTERVAL_TICKS = 4;
    private static final int MAX_MILESTONE_TARGETS = 10;
    private static final int UPGRADE_MILESTONE_LEVELS = 10;
    private static final int MAX_UTILITY_MILESTONES = 3;
    private static final int MAX_ORBITING_SOCKS = 10;
    private static final int DURATION_BONUS_PER_MILESTONE_TICKS = 100;
    private static final int ORBITING_SOCK_EXTENSION_TICKS = 100;
    private static final float RADIUS_BONUS_PER_MILESTONE = 1.0F;
    private static final int IMPACT_SOUND_DURATION_TICKS = 16;

    private static final EntityDataAccessor<Integer> DATA_OFFERING_LEVEL =
            SynchedEntityData.defineId(SockOfferingAltarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_TICKS =
            SynchedEntityData.defineId(SockOfferingAltarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ORBITING_SOCKS =
            SynchedEntityData.defineId(SockOfferingAltarEntity.class, EntityDataSerializers.INT);

    private UUID ownerUuid;
    private float spellPowerMultiplier = 1.0F;
    private int basicAttackCooldown;
    private int reactiveAttackCooldown;
    private int milestoneShotCooldown;
    private long nextImpactSoundGameTime;
    private final Queue<UUID> milestoneTargets = new ArrayDeque<>();

    public SockOfferingAltarEntity(EntityType<? extends SockOfferingAltarEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        noCulling = true;
    }

    public SockOfferingAltarEntity(Level level, LivingEntity owner, Vec3 position,
                                   int initialLevel, float spellPowerMultiplier) {
        this(ModEntities.SOCK_OFFERING_ALTAR.get(), level);
        ownerUuid = owner.getUUID();
        this.spellPowerMultiplier = Math.max(0.0F, spellPowerMultiplier);
        setOfferingLevel(initialLevel);
        setRemainingTicks(getMaxDurationTicks());
        basicAttackCooldown = getBasicAttackInterval();
        setPos(position.x, position.y, position.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OFFERING_LEVEL, 1);
        builder.define(DATA_REMAINING_TICKS, BASE_DURATION_TICKS);
        builder.define(DATA_ORBITING_SOCKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        setRemainingTicks(getRemainingTicks() - 1);
        if (getRemainingTicks() <= 0) {
            if (getOrbitingSockCount() > 0) {
                setOrbitingSockCount(getOrbitingSockCount() - 1);
                setRemainingTicks(ORBITING_SOCK_EXTENSION_TICKS);
                level().broadcastEntityEvent(this, (byte) 8);
            } else {
                discard();
                return;
            }
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity owner = getOwner(serverLevel);
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }

        if (reactiveAttackCooldown > 0) {
            reactiveAttackCooldown--;
        }
        tickMilestoneVolley(serverLevel, owner);

        if (basicAttackCooldown > 0) {
            basicAttackCooldown--;
        }
        if (basicAttackCooldown <= 0) {
            LivingEntity target = findNearestEnemy(serverLevel);
            if (target != null) {
                fireSock(serverLevel, owner, target, getBasicProjectileDamage());
            }
            basicAttackCooldown = getBasicAttackInterval();
        }
    }

    public void offer(Player player, int levels) {
        if (level().isClientSide() || levels <= 0 || !player.isAlive()) {
            return;
        }
        int oldLevel = getOfferingLevel();
        int newLevel = Math.min(MAX_OFFERING_LEVEL, oldLevel + levels);
        setOfferingLevel(newLevel);
        setRemainingTicks(getMaxDurationTicks());
        basicAttackCooldown = Math.min(basicAttackCooldown, getBasicAttackInterval());
        tryPlayImpactSound();
        level().broadcastEntityEvent(this, (byte) 7);

        if (level() instanceof ServerLevel serverLevel) {
            int oldMilestone = oldLevel / UPGRADE_MILESTONE_LEVELS;
            int newMilestone = newLevel / UPGRADE_MILESTONE_LEVELS;
            if (newMilestone > oldMilestone) {
                setOrbitingSockCount(getOrbitingSockCount() + newMilestone - oldMilestone);
            }
            for (int milestone = oldMilestone + 1; milestone <= newMilestone; milestone++) {
                queueMilestoneVolley(serverLevel);
            }
        }
    }

    public void tryReactiveShot(LivingEntity target) {
        if (!(level() instanceof ServerLevel serverLevel)
                || reactiveAttackCooldown > 0
                || !isEnemyInRange(target)) {
            return;
        }
        LivingEntity owner = getOwner(serverLevel);
        if (owner == null || !owner.isAlive()) {
            return;
        }
        fireSock(serverLevel, owner, target, getReactiveProjectileDamage());
        reactiveAttackCooldown = REACTIVE_COOLDOWN_TICKS;
    }

    private void tickMilestoneVolley(ServerLevel level, LivingEntity owner) {
        if (milestoneShotCooldown > 0) {
            milestoneShotCooldown--;
            return;
        }
        while (!milestoneTargets.isEmpty()) {
            UUID targetUuid = milestoneTargets.poll();
            Entity targetEntity = level.getEntity(targetUuid);
            if (targetEntity instanceof LivingEntity target && isEnemyInRange(target)) {
                SockOfferingProjectileEntity projectile = new SockOfferingProjectileEntity(
                        level, owner, target, getReactiveProjectileDamage());
                projectile.setPos(getProjectileOrigin());
                level.addFreshEntity(projectile);
                milestoneShotCooldown = MILESTONE_SHOT_INTERVAL_TICKS;
                return;
            }
        }
    }

    private void queueMilestoneVolley(ServerLevel level) {
        findEnemies(level).stream()
                .sorted(Comparator.comparingDouble(this::distanceToSqr))
                .limit(MAX_MILESTONE_TARGETS)
                .map(Entity::getUUID)
                .forEach(milestoneTargets::offer);
    }

    private void fireSock(ServerLevel level, LivingEntity owner, LivingEntity target, float damage) {
        HomingSockProjectileEntity projectile =
                new HomingSockProjectileEntity(level, owner, target, damage);
        projectile.setPos(getProjectileOrigin());
        level.addFreshEntity(projectile);
    }

    private Vec3 getProjectileOrigin() {
        return position().add(0.0D, 1.45D, 0.0D);
    }

    private LivingEntity findNearestEnemy(ServerLevel level) {
        return findEnemies(level).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private List<LivingEntity> findEnemies(ServerLevel level) {
        AABB area = getBoundingBox().inflate(getAttackRadius());
        return level.getEntitiesOfClass(LivingEntity.class, area, this::isEnemyInRange);
    }

    private boolean isEnemyInRange(LivingEntity entity) {
        float attackRadius = getAttackRadius();
        return entity instanceof Enemy
                && entity.isAlive()
                && !entity.isSpectator()
                && distanceToSqr(entity) <= attackRadius * attackRadius;
    }

    private LivingEntity getOwner(ServerLevel level) {
        Entity entity = ownerUuid == null ? null : level.getEntity(ownerUuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    public int getOfferingLevel() {
        return entityData.get(DATA_OFFERING_LEVEL);
    }

    public void setOfferingLevel(int level) {
        entityData.set(DATA_OFFERING_LEVEL,
                Math.max(1, Math.min(MAX_OFFERING_LEVEL, level)));
    }

    public int getRemainingTicks() {
        return entityData.get(DATA_REMAINING_TICKS);
    }

    public void setRemainingTicks(int ticks) {
        entityData.set(DATA_REMAINING_TICKS, Math.max(0, ticks));
    }

    public int getQuality() {
        return Math.min(5, getOfferingLevel());
    }

    public float getBasicProjectileDamage() {
        return getBasicProjectileDamage(getQuality(), spellPowerMultiplier);
    }

    public float getReactiveProjectileDamage() {
        return getReactiveProjectileDamage(getQuality());
    }

    public int getBasicAttackInterval() {
        return getBasicAttackInterval(getQuality());
    }

    public float getAttackRadius() {
        return getAttackRadius(getQuality())
                + getUtilityMilestoneCount() * RADIUS_BONUS_PER_MILESTONE;
    }

    public int getMaxDurationTicks() {
        return BASE_DURATION_TICKS
                + getUtilityMilestoneCount() * DURATION_BONUS_PER_MILESTONE_TICKS;
    }

    public int getOrbitingSockCount() {
        return entityData.get(DATA_ORBITING_SOCKS);
    }

    public void setOrbitingSockCount(int count) {
        entityData.set(DATA_ORBITING_SOCKS,
                Math.max(0, Math.min(MAX_ORBITING_SOCKS, count)));
    }

    public static float getBasicProjectileDamage(int quality, float spellPowerMultiplier) {
        int index = qualityIndex(quality);
        return (BASE_PROJECTILE_DAMAGE + QUALITY_DAMAGE_BONUS[index])
                * Math.max(0.0F, spellPowerMultiplier);
    }

    public static float getReactiveProjectileDamage(int quality) {
        return REACTIVE_DAMAGE[qualityIndex(quality)];
    }

    public static int getBasicAttackInterval(int quality) {
        return BASIC_ATTACK_INTERVAL[qualityIndex(quality)];
    }

    public static float getAttackRadius(int quality) {
        return ATTACK_RADIUS[qualityIndex(quality)];
    }

    private int getUtilityMilestoneCount() {
        return Math.min(MAX_UTILITY_MILESTONES,
                getOfferingLevel() / UPGRADE_MILESTONE_LEVELS);
    }

    private static int qualityIndex(int quality) {
        return Math.max(0, Math.min(QUALITY_DAMAGE_BONUS.length - 1, quality - 1));
    }

    private void tryPlayImpactSound() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        long gameTime = serverLevel.getGameTime();
        if (gameTime < nextImpactSoundGameTime) {
            return;
        }
        serverLevel.playSound(null, getX(), getY() + 1.0D, getZ(),
                ModSounds.SOCK_OFFERING_IMPACT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        nextImpactSoundGameTime = gameTime + IMPACT_SOUND_DURATION_TICKS;
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 7) {
            for (int i = 0; i < 18; i++) {
                double angle = Math.PI * 2.0D * i / 18.0D;
                level().addParticle(ParticleTypes.END_ROD,
                        getX(), getY() + 1.35D, getZ(),
                        Math.cos(angle) * 0.08D, 0.035D, Math.sin(angle) * 0.08D);
            }
        } else if (id == 8) {
            for (int i = 0; i < 40; i++) {
                double angle = level().random.nextDouble() * Math.PI * 2.0D;
                double speed = 0.08D + level().random.nextDouble() * 0.14D;
                level().addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                        getX(), getY() + 0.9D + level().random.nextDouble() * 0.8D, getZ(),
                        Math.cos(angle) * speed,
                        0.05D + level().random.nextDouble() * 0.16D,
                        Math.sin(angle) * speed);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player)) {
            return false;
        }
        tryPlayImpactSound();
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putInt("OfferingLevel", getOfferingLevel());
        tag.putInt("RemainingTicks", getRemainingTicks());
        tag.putInt("OrbitingSockCount", getOrbitingSockCount());
        tag.putFloat("SpellPowerMultiplier", spellPowerMultiplier);
        tag.putInt("BasicAttackCooldown", basicAttackCooldown);
        tag.putInt("ReactiveAttackCooldown", reactiveAttackCooldown);
        tag.putInt("MilestoneShotCooldown", milestoneShotCooldown);
        tag.putLong("NextImpactSoundGameTime", nextImpactSoundGameTime);
        int index = 0;
        for (UUID target : milestoneTargets) {
            tag.putUUID("MilestoneTarget" + index++, target);
        }
        tag.putInt("MilestoneTargetCount", index);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        setOfferingLevel(tag.getInt("OfferingLevel"));
        setRemainingTicks(tag.getInt("RemainingTicks"));
        setOrbitingSockCount(tag.contains("OrbitingSockCount")
                ? tag.getInt("OrbitingSockCount")
                : getOfferingLevel() / UPGRADE_MILESTONE_LEVELS);
        spellPowerMultiplier = tag.contains("SpellPowerMultiplier")
                ? Math.max(0.0F, tag.getFloat("SpellPowerMultiplier")) : 1.0F;
        basicAttackCooldown = Math.max(0, tag.getInt("BasicAttackCooldown"));
        reactiveAttackCooldown = Math.max(0, tag.getInt("ReactiveAttackCooldown"));
        milestoneShotCooldown = Math.max(0, tag.getInt("MilestoneShotCooldown"));
        nextImpactSoundGameTime = Math.max(0L, tag.getLong("NextImpactSoundGameTime"));
        milestoneTargets.clear();
        int targetCount = Math.min(100, Math.max(0, tag.getInt("MilestoneTargetCount")));
        for (int i = 0; i < targetCount; i++) {
            String key = "MilestoneTarget" + i;
            if (tag.hasUUID(key)) {
                milestoneTargets.offer(tag.getUUID(key));
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }
}
