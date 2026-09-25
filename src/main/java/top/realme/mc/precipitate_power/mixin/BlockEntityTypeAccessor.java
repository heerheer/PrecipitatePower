package top.realme.mc.precipitate_power.mixin;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
    @Accessor("validBlocks")
    Set<Block> precipitatePower$getValidBlocks();

    @Mutable
    @Accessor("validBlocks")
    void precipitatePower$setValidBlocks(Set<Block> validBlocks);
}
