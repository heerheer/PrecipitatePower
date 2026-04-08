package top.realme.mc.precipitate_power.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Math;
import top.realme.mc.precipitate_power.menu.PrecipitateGeneratorMenu;

public class PrecipitateGeneratorScreen extends AbstractContainerScreen<PrecipitateGeneratorMenu> {
    public PrecipitateGeneratorScreen(PrecipitateGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1E2026);
        guiGraphics.fill(x + 6, y + 17, x + 170, y + 71, 0xFF2A2D36);
        guiGraphics.fill(x + 43, y + 34, x + 61, y + 52, 0xFF4B5160);
        guiGraphics.fill(x + 115, y + 34, x + 133, y + 52, 0xFF4B5160);
        guiGraphics.fill(x + 80, y + 18, x + 96, y + 70, 0xFF16181D);
        if (menu.getMaxWaterStored() > 0) {
            guiGraphics.fill(x + 62, y + 18, x + 78, y + 70, 0xFF16181D);
        }

        int energy = menu.getEnergyStored();
        int capacity = Math.max(1, menu.getMaxEnergyStored());
        int height = Mth.clamp((int) ((energy / (float) capacity) * 50), 0, 50);
        guiGraphics.fill(x + 82, y + 68 - height, x + 94, y + 68, 0xFF46C266);

        if (menu.getMaxWaterStored() > 0) {
            int waterHeight = Mth.clamp((int) ((menu.getWaterStored() / (float) menu.getMaxWaterStored()) * 50), 0, 50);
            guiGraphics.fill(x + 64, y + 68 - waterHeight, x + 76, y + 68, 0xFF3B82F6);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        //guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xF0F0F0, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xC7CBD6, false);
        guiGraphics.drawString(font, Component.translatable("gui.precipitate_power.energy", menu.getEnergyStored(), menu.getMaxEnergyStored()), 8, 6, 0x8ED0A7, false);
        if (menu.getMaxWaterStored() > 0) {
            guiGraphics.drawString(font, Component.translatable("gui.precipitate_power.water", menu.getWaterStored(), menu.getMaxWaterStored()), 8, 18, 0x7DB7FF, false);
        }
//        guiGraphics.drawString(font, Component.translatable("gui.precipitate_power.precipitation", menu.getPrecipitationLevel()), 8, 18, 0x8AC7FF, false);
//        guiGraphics.drawString(font, Component.translatable("gui.precipitate_power.dirty_count", menu.getDirtyCount()), 8, 30, 0xD7DCE6, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
