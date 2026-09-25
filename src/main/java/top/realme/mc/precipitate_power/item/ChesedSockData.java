package top.realme.mc.precipitate_power.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record ChesedSockData(
        int level,
        int cheeseProgress,
        int damage,
        int additionalDamage,
        int additionalHits,
        int damagePercent,
        int attackRange,
        State state
) {
    public static final int MAX_LEVEL = 31;
    public static final ChesedSockData DEFAULT = new ChesedSockData(0, 0, 0, 0, 0, 0, 0, State.GATE);
    public static final Codec<ChesedSockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(ChesedSockData::level),
            Codec.INT.fieldOf("cheese_progress").forGetter(ChesedSockData::cheeseProgress),
            Codec.INT.fieldOf("damage").forGetter(ChesedSockData::damage),
            Codec.INT.fieldOf("additional_damage").forGetter(ChesedSockData::additionalDamage),
            Codec.INT.fieldOf("additional_hits").forGetter(ChesedSockData::additionalHits),
            Codec.INT.fieldOf("damage_percent").forGetter(ChesedSockData::damagePercent),
            Codec.INT.fieldOf("attack_range").forGetter(ChesedSockData::attackRange),
            State.CODEC.fieldOf("state").forGetter(ChesedSockData::state)
    ).apply(instance, ChesedSockData::new));

    public ChesedSockData {
        level = Mth.clamp(level, 0, MAX_LEVEL);
        cheeseProgress = Math.max(0, cheeseProgress);
        damage = Math.max(0, damage);
        additionalDamage = Math.max(0, additionalDamage);
        additionalHits = Math.max(0, additionalHits);
        damagePercent = Math.max(0, damagePercent);
        attackRange = Math.max(0, attackRange);
        state = state == null ? State.GATE : state;
    }

    public int requiredCheese() {
        return this.level >= MAX_LEVEL ? Integer.MAX_VALUE : 1 << this.level;
    }

    public boolean canFeed() {
        return this.level < MAX_LEVEL;
    }

    public FeedResult feed(RandomSource random) {
        return feed(random, 1);
    }

    public FeedResult feed(RandomSource random, int cheeseCount) {
        ChesedSockData current = this;
        long remaining = Math.max(0, cheeseCount);
        int levelsGained = 0;
        while (remaining > 0 && current.canFeed()) {
            int needed = current.requiredCheese() - current.cheeseProgress;
            if (remaining < needed) {
                current = new ChesedSockData(
                        current.level, current.cheeseProgress + (int) remaining,
                        current.damage, current.additionalDamage, current.additionalHits,
                        current.damagePercent, current.attackRange, current.state);
                remaining = 0;
                break;
            }
            remaining -= Math.max(0, needed);
            current = current.upgrade(random);
            levelsGained++;
        }
        return new FeedResult(current, levelsGained);
    }

    private ChesedSockData upgrade(RandomSource random) {
        int upgradedLevel = this.level + 1;
        int upgradedDamage = this.damage;
        int upgradedAdditionalDamage = this.additionalDamage;
        int upgradedAdditionalHits = this.additionalHits;
        int upgradedDamagePercent = this.damagePercent;
        int upgradedAttackRange = this.attackRange;
        double roll = random.nextDouble();

        if (roll < 0.30D) {
            upgradedDamage += scaledRandom(random, 2, 10, upgradedLevel);
        } else if (roll < 0.60D) {
            upgradedAdditionalDamage += scaledRandom(random, 1, 4, upgradedLevel);
        } else if (roll < 0.75D) {
            upgradedAdditionalHits += upgradedLevel > 10 ? 2 : 1;
        } else if (roll < 0.95D) {
            upgradedDamagePercent += scaledRandom(random, 5, 10, upgradedLevel);
        } else {
            upgradedAttackRange += 1;
        }

        return new ChesedSockData(
                upgradedLevel, 0, upgradedDamage, upgradedAdditionalDamage, upgradedAdditionalHits,
                upgradedDamagePercent, upgradedAttackRange, this.state);
    }

    public ChesedSockData switchState(RandomSource random) {
        State[] states = State.values();
        State next = states[random.nextInt(states.length - 1)];
        if (next.ordinal() >= this.state.ordinal()) {
            next = states[next.ordinal() + 1];
        }
        return new ChesedSockData(this.level, this.cheeseProgress, this.damage, this.additionalDamage,
                this.additionalHits, this.damagePercent, this.attackRange, next);
    }

    private static int scaledRandom(RandomSource random, int min, int max, int upgradedLevel) {
        int scaledMin = min + (max - min) * Math.max(0, upgradedLevel - 1) / (MAX_LEVEL - 1);
        return Mth.nextInt(random, scaledMin, max);
    }

    public record FeedResult(ChesedSockData data, int levelsGained) {
        public boolean upgraded() {
            return levelsGained > 0;
        }
    }

    public enum State {
        GATE,
        BEAR_HOTEL,
        SIDE,
        HALF_ZERO_DO_ONE,
        HALF_ONE_DO_ZERO;

        public static final Codec<State> CODEC = Codec.STRING.xmap(
                value -> State.valueOf(value.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }
}
