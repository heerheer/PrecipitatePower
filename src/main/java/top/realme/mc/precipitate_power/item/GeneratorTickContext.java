package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import top.realme.mc.precipitate_power.block.entity.AbstractPrecipitateGeneratorBlockEntity;

public final class GeneratorTickContext {
    private final ServerLevel level;
    private final AbstractPrecipitateGeneratorBlockEntity generator;
    private final ItemStack inputStack;

    public GeneratorTickContext(ServerLevel level, AbstractPrecipitateGeneratorBlockEntity generator, ItemStack inputStack) {
        this.level = level;
        this.generator = generator;
        this.inputStack = inputStack;
    }

    public ServerLevel level() {
        return level;
    }

    public AbstractPrecipitateGeneratorBlockEntity generator() {
        return generator;
    }

    public ItemStack inputStack() {
        return inputStack;
    }

    public RandomSource random() {
        return level.random;
    }

    public BlockPos pos() {
        return generator.getBlockPos();
    }

    public int precipitation() {
        return top.realme.mc.precipitate_power.util.SockDataUtil.getPrecipitationLevel(inputStack);
    }

    public int dirtyCount() {
        return top.realme.mc.precipitate_power.util.SockDataUtil.getDirtyCount(inputStack);
    }

    public int energyStored() {
        return generator.getEnergyStorage().getEnergyStored();
    }

    public int energyCapacity() {
        return generator.getEnergyStorage().getMaxEnergyStored();
    }

    public int maxExtract() {
        return generator.getMaxExtract();
    }

    public double generationMultiplier() {
        return generator.getGenerationMultiplierForItems();
    }

    public double dirtyChanceMultiplier() {
        return generator.getDirtyChanceMultiplierForItems();
    }

    public boolean canConsumeGenerationResource(int precipitation) {
        return generator.canConsumeGenerationResourceForItems(precipitation);
    }

    public void consumeGenerationResource(int precipitation) {
        generator.consumeGenerationResourceForItems(precipitation);
    }

    public void markDirty() {
        generator.setChanged();
    }

    public boolean consumeStoredEnergy(int amount) {
        return generator.consumeStoredEnergy(amount);
    }

    public List<LivingEntity> findNearbyLivingEntities(double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(pos()).inflate(radius));
    }

    public void spawnParticles(ParticleOptions particle, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        level.sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
    }
}
