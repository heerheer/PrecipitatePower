package top.realme.mc.precipitate_power.spell;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class SockTargetCastData implements ICastDataSerializable {
    private UUID targetUuid;

    public SockTargetCastData() {
    }

    public SockTargetCastData(LivingEntity target) {
        targetUuid = target.getUUID();
    }

    public LivingEntity getTarget(ServerLevel level) {
        if (targetUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(targetUuid);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    public void reset() {
        targetUuid = null;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(targetUuid != null);
        if (targetUuid != null) {
            buffer.writeUUID(targetUuid);
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        targetUuid = buffer.readBoolean() ? buffer.readUUID() : null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (targetUuid != null) {
            tag.putUUID("Target", targetUuid);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        targetUuid = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
    }
}
