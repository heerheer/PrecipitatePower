package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class BoatSockItem extends WhiteSockItem {
    public BoatSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        SockDataUtil.initializeBoatSock(stack, 1);
        return stack;
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        ItemStack stack = context.inputStack();
        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        int generated = super.calculateBaseGeneration(context, precipitation);
        generated = SockDataUtil.applyMaterialGenerationFlatBonus(stack, generated);
        generated = SockDataUtil.applyMaterialGenerationMultiplier(stack, generated);
        boolean changed = false;

        if (generated > 0 && context.canConsumeGenerationResource(precipitation)) {
            context.consumeGenerationResource(precipitation);
            changed = true;
        } else {
            generated = 0;
        }

        if (context.level().getGameTime() % 20L == 0L) {
            int multiplier = consumeDurabilityWithModifiers(stack, context.level());
            if (multiplier > 0) {
                context.generator().addExtraCapacity(SockDataUtil.getBoatSockCapacityBoost(stack) * multiplier);
            }
            changed = true;
        }
        return GeneratorTickResult.handled(generated, 0, changed, stack.isEmpty() ? ItemStack.EMPTY : stack, ItemStack.EMPTY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            tooltipComponents.add(Component.translatable(
                    "tooltip.precipitate_power.boat_sock.detail",
                    SockDataUtil.getBoatSockCapacityBoost(stack)
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.boat_sock.summary").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.travel_disposable_sock.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
