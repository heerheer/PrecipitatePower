package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.realme.mc.precipitate_power.Config;
import top.realme.mc.precipitate_power.util.SockDataUtil;

public class TravelDisposableSockItem extends WhiteSockItem {
    public TravelDisposableSockItem(Properties properties) {
        super(properties);
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        ItemStack stack = context.inputStack();
        int precipitation = SockDataUtil.getPrecipitationLevel(stack);
        int generated = (int) Math.max(0, Math.floor(super.calculateBaseGeneration(context, precipitation) * Config.TRAVEL_SOCK_GENERATION_MULTIPLIER.get()));
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
            if (multiplier > 0 && stack.isEmpty()) {
                context.generator().addExtraMaxExtract(Config.TRAVEL_SOCK_MAX_EXTRACT_BOOST.get() * multiplier);
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
                    "tooltip.precipitate_power.travel_disposable_sock.detail",
                    Config.TRAVEL_SOCK_GENERATION_MULTIPLIER.get(),
                    Config.TRAVEL_SOCK_MAX_EXTRACT_BOOST.get()
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.travel_disposable_sock.summary").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.travel_disposable_sock.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
