package top.realme.mc.precipitate_power.registry;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import top.realme.mc.precipitate_power.PrecipitatePower;

public final class ModAdvancements {
    public static final ResourceLocation THE_REAL_TASTE = ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "the_real_taste");
    public static final ResourceLocation ULTIMATE_BLENDER = ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "ultimate_blender");
    public static final ResourceLocation IT_WAS_FUSHENG = ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "it_was_fusheng");
    public static final ResourceLocation POWER_BELONGS_TO_DT = ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "power_belongs_to_dt");
    public static final ResourceLocation PLAYERS_CAN_GENERATE_POWER = ResourceLocation.fromNamespaceAndPath(PrecipitatePower.MODID, "players_can_generate_power");

    private ModAdvancements() {
    }

    public static void grant(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement == null) {
            return;
        }
        PlayerAdvancements advancements = player.getAdvancements();
        advancements.getOrStartProgress(advancement).getRemainingCriteria().forEach(criteria -> advancements.award(advancement, criteria));
    }
}
