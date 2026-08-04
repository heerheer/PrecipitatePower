package top.realme.mc.precipitate_power.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public record BurningBananaData(int heat, int explosions) {
    public static final BurningBananaData DEFAULT = new BurningBananaData(0, 0);
    public static final Codec<BurningBananaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("heat").forGetter(BurningBananaData::heat),
            Codec.INT.fieldOf("explosions").forGetter(BurningBananaData::explosions)
    ).apply(instance, BurningBananaData::new));

    public BurningBananaData {
        heat = Mth.clamp(heat, 0, BurningBananaItem.HEAT_REQUIRED_FOR_BURST - 1);
        explosions = Math.max(0, explosions);
    }

    public BurningBananaData withHeat(int newHeat) {
        return new BurningBananaData(newHeat, explosions);
    }

    public BurningBananaData afterBurst() {
        return new BurningBananaData(0, explosions + 1);
    }
}
