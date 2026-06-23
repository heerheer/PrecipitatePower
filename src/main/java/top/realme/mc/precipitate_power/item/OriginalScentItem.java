package top.realme.mc.precipitate_power.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class OriginalScentItem extends AbstractSockItem {
    private final String tooltipKeyPrefix;

    public OriginalScentItem(Properties properties, String tooltipKeyPrefix) {
        super(properties);
        this.tooltipKeyPrefix = tooltipKeyPrefix;
    }

    protected String tooltipKeyPrefix() {
        return this.tooltipKeyPrefix;
    }

    @Override
    public boolean isWearableSock(ItemStack stack) {
        return false;
    }

    @Override
    public boolean rollMaterialsOnGeneration() {
        return false;
    }

    @Override
    public GeneratorTickResult tickInGenerator(GeneratorTickContext context) {
        return GeneratorTickResult.handled(0, 0, false, context.inputStack(), ItemStack.EMPTY);
    }

    protected void appendExtraShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(this.tooltipKeyPrefix + ".summary").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.translatable(this.tooltipKeyPrefix + ".detail").withStyle(ChatFormatting.GRAY));
        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.translatable(this.tooltipKeyPrefix + ".shift_detail").withStyle(ChatFormatting.AQUA));
            appendExtraShiftTooltip(stack, context, tooltipComponents, tooltipFlag);
        } else {
            tooltipComponents.add(Component.translatable("tooltip.precipitate_power.original_scent.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
