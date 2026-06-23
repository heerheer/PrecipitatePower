package top.realme.mc.precipitate_power.item;

import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.realme.mc.precipitate_power.registry.ModAdvancements;

public class FushengOriginalScentItem extends OriginalScentItem {
    private static final String TAG_OWNER_UUID = "FushengOwnerUUID";
    private static final String TAG_GROWTH_COUNT = "FushengGrowthCount";
    private static final int ADVANCEMENT_TARGET = 100;

    public FushengOriginalScentItem(Properties properties) {
        super(properties, "tooltip.precipitate_power.fusheng_original_scent");
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        if (context.level().getGameTime() % 20L == 0L) {
            context.level().levelEvent(1505, context.pos().above(), 15);
        }
        return GeneratorTickResult.handled(0, 0, false, context.inputStack(), ItemStack.EMPTY);
    }

    @Override
    protected void appendExtraShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(tooltipKeyPrefix() + ".shift_effect").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(tooltipKeyPrefix() + ".growth_count", getGrowthCount(stack), ADVANCEMENT_TARGET).withStyle(ChatFormatting.GREEN));
    }

    public static void bindOwner(ItemStack stack, UUID ownerUuid) {
        updateData(stack, tag -> tag.putUUID(TAG_OWNER_UUID, ownerUuid));
    }

    public static int getGrowthCount(ItemStack stack) {
        return Math.max(0, getData(stack).getInt(TAG_GROWTH_COUNT));
    }

    public static void tickDroppedItem(ServerLevel level, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!hasOwner(stack)) {
            return;
        }

        BlockPos farmlandPos = findFarmland(level, itemEntity.blockPosition());
        if (farmlandPos == null) {
            return;
        }

        BlockPos cropPos = farmlandPos.above();
        BlockState cropState = level.getBlockState(cropPos);
        if (cropState.isAir()) {
            return;
        }

        level.levelEvent(1505, cropPos, 15);
        if (tryHarvestMatureCrop(level, cropPos, cropState)) {
            incrementGrowthCount(level, stack);
            return;
        }
        if (tryBonemealCrop(level, cropPos, cropState)) {
            incrementGrowthCount(level, stack);
        }
    }

    private static boolean hasOwner(ItemStack stack) {
        return getData(stack).hasUUID(TAG_OWNER_UUID);
    }

    private static void incrementGrowthCount(ServerLevel level, ItemStack stack) {
        int updated = getGrowthCount(stack) + 1;
        updateData(stack, tag -> tag.putInt(TAG_GROWTH_COUNT, updated));
        if (updated < ADVANCEMENT_TARGET) {
            return;
        }

        UUID ownerUuid = getData(stack).getUUID(TAG_OWNER_UUID);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner != null) {
            ModAdvancements.grant(owner, ModAdvancements.IT_WAS_FUSHENG);
        }
    }

    private static BlockPos findFarmland(ServerLevel level, BlockPos itemPos) {
        for (int offset = 0; offset <= 2; offset++) {
            BlockPos candidate = itemPos.below(offset);
            if (level.getBlockState(candidate).getBlock() instanceof FarmBlock) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean tryHarvestMatureCrop(ServerLevel level, BlockPos cropPos, BlockState cropState) {
        if (!(cropState.getBlock() instanceof CropBlock cropBlock) || !cropBlock.isMaxAge(cropState)) {
            return false;
        }

        for (ItemStack drop : Block.getDrops(cropState, level, cropPos, null)) {
            Block.popResource(level, cropPos, drop);
        }
        level.setBlock(cropPos, cropBlock.getStateForAge(0), Block.UPDATE_ALL);
        return true;
    }

    private static boolean tryBonemealCrop(ServerLevel level, BlockPos cropPos, BlockState cropState) {
        if (cropState.getBlock() instanceof BonemealableBlock bonemealable
                && bonemealable.isValidBonemealTarget(level, cropPos, cropState)
                && bonemealable.isBonemealSuccess(level, level.random, cropPos, cropState)) {
            bonemealable.performBonemeal(level, level.random, cropPos, cropState);
            return true;
        }
        return false;
    }

    private static CompoundTag getData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void updateData(ItemStack stack, java.util.function.Consumer<CompoundTag> consumer) {
        CustomData updated = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(consumer);
        stack.set(DataComponents.CUSTOM_DATA, updated);
    }
}
