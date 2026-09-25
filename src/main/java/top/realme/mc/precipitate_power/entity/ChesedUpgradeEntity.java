package top.realme.mc.precipitate_power.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import top.realme.mc.precipitate_power.registry.ModEntities;

public class ChesedUpgradeEntity extends Entity {
    public static final int DURATION_TICKS = 60;
    private static final int RETURN_DURATION_TICKS = 15;
    private static final double DISPLAY_DISTANCE = 1.75D;
    private static final double RETURN_DISTANCE = 0.35D;
    private static final EntityDataAccessor<ItemStack> SOCK = SynchedEntityData.defineId(
            ChesedUpgradeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> CHEESE = SynchedEntityData.defineId(
            ChesedUpgradeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> CHEESE_COUNT = SynchedEntityData.defineId(
            ChesedUpgradeEntity.class, EntityDataSerializers.INT);
    @Nullable
    private UUID owner;

    public ChesedUpgradeEntity(EntityType<? extends ChesedUpgradeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public static boolean spawn(ServerLevel level, ServerPlayer owner, ItemStack sock, ItemStack cheese) {
        ChesedUpgradeEntity entity = new ChesedUpgradeEntity(ModEntities.CHESED_UPGRADE.get(), level);
        entity.moveInFrontOf(owner, DISPLAY_DISTANCE);
        entity.owner = owner.getUUID();
        entity.setSock(sock);
        entity.setCheese(cheese);
        entity.setCheeseCount(5 + level.random.nextInt(6));
        return level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SOCK, ItemStack.EMPTY);
        builder.define(CHEESE, ItemStack.EMPTY);
        builder.define(CHEESE_COUNT, 5);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (owner != null && this.level() instanceof ServerLevel serverLevel) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(owner);
            if (player != null) {
                float returnProgress = Mth.clamp(
                        (this.tickCount - (DURATION_TICKS - RETURN_DURATION_TICKS)) / (float) RETURN_DURATION_TICKS,
                        0.0F, 1.0F);
                moveInFrontOf(player, Mth.lerp(returnProgress, DISPLAY_DISTANCE, RETURN_DISTANCE));
            }
        }
        if (this.tickCount == DURATION_TICKS - 1) {
            this.level().broadcastEntityEvent(this, (byte) 4);
            this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.35F);
        }
        if (this.tickCount >= DURATION_TICKS) {
            finish();
        }
    }

    private void finish() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        ItemStack sock = getSock();
        Player player = owner == null ? null : serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.isAlive()) {
            serverPlayer.getInventory().placeItemBackInInventory(sock);
        } else if (!sock.isEmpty()) {
            serverLevel.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    serverLevel, getX(), getY(), getZ(), sock));
        }
        discard();
    }

    private void moveInFrontOf(ServerPlayer player, double distance) {
        Vec3 position = player.getEyePosition().add(player.getLookAngle().scale(distance));
        setPos(position.x, position.y, position.z);
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 4) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLASH,
                    getX(), getY() + 0.2D, getZ(), 0.0D, 0.0D, 0.0D);
            for (int i = 0; i < 24; i++) {
                double angle = Math.PI * 2.0D * i / 24.0D;
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        getX(), getY() + 0.2D, getZ(), Math.cos(angle) * 0.12D, 0.04D, Math.sin(angle) * 0.12D);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        if (!getSock().isEmpty()) {
            tag.put("Sock", getSock().save(this.registryAccess()));
        }
        if (!getCheese().isEmpty()) {
            tag.put("Cheese", getCheese().save(this.registryAccess()));
        }
        tag.putInt("CheeseCount", getCheeseCount());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        setSock(tag.contains("Sock", 10) ? ItemStack.parse(this.registryAccess(), tag.getCompound("Sock")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY);
        setCheese(tag.contains("Cheese", 10) ? ItemStack.parse(this.registryAccess(), tag.getCompound("Cheese")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY);
        setCheeseCount(Math.max(5, Math.min(10, tag.getInt("CheeseCount"))));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public ItemStack getSock() {
        return this.entityData.get(SOCK);
    }

    public void setSock(ItemStack stack) {
        this.entityData.set(SOCK, stack);
    }

    public ItemStack getCheese() {
        return this.entityData.get(CHEESE);
    }

    public void setCheese(ItemStack stack) {
        this.entityData.set(CHEESE, stack);
    }

    public int getCheeseCount() {
        return this.entityData.get(CHEESE_COUNT);
    }

    public void setCheeseCount(int count) {
        this.entityData.set(CHEESE_COUNT, count);
    }
}
