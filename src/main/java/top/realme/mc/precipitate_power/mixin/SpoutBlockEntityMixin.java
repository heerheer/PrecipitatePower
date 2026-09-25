package top.realme.mc.precipitate_power.mixin;

import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.realme.mc.precipitate_power.item.ChesedOriginalScentItem;
import top.realme.mc.precipitate_power.item.ChesedSockData;
import top.realme.mc.precipitate_power.registry.ModFluids;

@Mixin(SpoutBlockEntity.class)
public abstract class SpoutBlockEntityMixin {
    private static final int CHEESE_PER_LEVEL_PROGRESS = 1;

    @Redirect(
            method = "whenItemHeld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/spout/FillingBySpout;fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack precipitatePower$fillChesedOriginalScent(
            Level level, int requiredAmount, ItemStack input, FluidStack availableFluid) {
        if (!(input.getItem() instanceof ChesedOriginalScentItem)
                || !availableFluid.getFluid().isSame(ModFluids.CONCENTRATED_FRESH_PRESSED_CHEESE.get())) {
            return FillingBySpout.fillItem(level, requiredAmount, input, availableFluid);
        }

        ChesedSockData current = ChesedOriginalScentItem.getData(input);
        if (!current.canFeed()) {
            return FillingBySpout.fillItem(level, requiredAmount, input, availableFluid);
        }

        ItemStack resultStack = input.copyWithCount(1);
        ChesedSockData.FeedResult result = current.feed(level.random, CHEESE_PER_LEVEL_PROGRESS);
        ChesedOriginalScentItem.setData(resultStack, result.data());
        availableFluid.shrink(requiredAmount);
        input.shrink(1);

        if (result.upgraded() && level instanceof ServerLevel serverLevel) {
            spawnUpgradeParticles(serverLevel, ((SpoutBlockEntity) (Object) this).getBlockPos());
        }
        return resultStack;
    }

    private static void spawnUpgradeParticles(ServerLevel level, BlockPos spoutPos) {
        double x = spoutPos.getX() + 0.5D;
        double y = spoutPos.getY() - 1.2D;
        double z = spoutPos.getZ() + 0.5D;
        Vector3f[] colors = {
                new Vector3f(1.0F, 0.2F, 0.2F),
                new Vector3f(1.0F, 0.85F, 0.15F),
                new Vector3f(0.2F, 1.0F, 0.45F),
                new Vector3f(0.25F, 0.55F, 1.0F),
                new Vector3f(0.85F, 0.3F, 1.0F)
        };
        for (Vector3f color : colors) {
            level.sendParticles(new DustParticleOptions(color, 1.2F), x, y, z,
                    8, 0.25D, 0.2D, 0.25D, 0.08D);
        }
        level.sendParticles(ParticleTypes.FIREWORK, x, y, z,
                24, 0.3D, 0.25D, 0.3D, 0.12D);
    }
}
