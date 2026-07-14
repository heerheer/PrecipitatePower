package top.realme.mc.precipitate_power.item;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum SockMaterial {
    COTTON("cotton", 0.70D, 0.10D, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    WOOL("wool", 0.70D, 0.0D, 0.0D, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    NYLON("nylon", 0.70D, 0.0D, 0.0D, 0.0D, 0.20D, 0.0D, 0.0D, 0.0D, 0.0D, 0.15D),
    BAMBOO("bamboo", 0.70D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 0.05D, 0.0D),
    POLYESTER("polyester", 0.70D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.20D, 0.0D, 0.0D, 0.0D),
    IRON("iron", 0.30D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.10D, 0.0D, 0.0D),
    DIAMOND("diamond", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    SILK("silk", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    FLESH("flesh", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    GOLD("gold", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.15D, 0.0D, 0.0D),
    MITHRIL_WEAVE("mithril_weave", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    ARCANE_CLOTH("arcane_cloth", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    SNIFFER_FUR("sniffer_fur", 0.70D, 0.10D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    GOLDEN_FABRIC("golden_fabric", 0.30D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D),
    ANCIENT_FIBER("ancient_fiber", 0.30D, -0.05D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    public static final List<SockMaterial> VALUES = List.of(values());

    private final String id;
    private final double poolWeight;
    private final double movementSpeedBonus;
    private final double attackDamageBonus;
    private final double maxHealthBonus;
    private final double knockbackResistanceBonus;
    private final double jumpBoostBonus;
    private final double swimSpeedBonus;
    private final double generationMultiplierBonus;
    private final double repairChance;
    private final double nylonNoDamageChance;

    SockMaterial(
            String id,
            double poolWeight,
            double movementSpeedBonus,
            double attackDamageBonus,
            double maxHealthBonus,
            double knockbackResistanceBonus,
            double jumpBoostBonus,
            double swimSpeedBonus,
            double generationMultiplierBonus,
            double repairChance,
            double nylonNoDamageChance
    ) {
        this.id = id;
        this.poolWeight = poolWeight;
        this.movementSpeedBonus = movementSpeedBonus;
        this.attackDamageBonus = attackDamageBonus;
        this.maxHealthBonus = maxHealthBonus;
        this.knockbackResistanceBonus = knockbackResistanceBonus;
        this.jumpBoostBonus = jumpBoostBonus;
        this.swimSpeedBonus = swimSpeedBonus;
        this.generationMultiplierBonus = generationMultiplierBonus;
        this.repairChance = repairChance;
        this.nylonNoDamageChance = nylonNoDamageChance;
    }

    public String id() {
        return id;
    }

    public double poolWeight() {
        return poolWeight;
    }

    public double movementSpeedBonus() {
        return movementSpeedBonus;
    }

    public double attackDamageBonus() {
        return attackDamageBonus;
    }

    public double maxHealthBonus() {
        return maxHealthBonus;
    }

    public double knockbackResistanceBonus() {
        return knockbackResistanceBonus;
    }

    public double jumpBoostBonus() {
        return jumpBoostBonus;
    }

    public double swimSpeedBonus() {
        return swimSpeedBonus;
    }

    public double generationMultiplierBonus() {
        return generationMultiplierBonus;
    }

    public double repairChance() {
        return repairChance;
    }

    public double nylonNoDamageChance() {
        return nylonNoDamageChance;
    }

    public double generationFlatBonus() {
        return this == COTTON ? 2.0D : 0.0D;
    }

    public double armorBonus() {
        return this == DIAMOND ? 2.0D : 0.0D;
    }

    public double luckBonus() {
        return this == GOLD ? 1.0D : 0.0D;
    }

    public double diamondDurabilityMultiplier() {
        return this == DIAMOND ? 1.0D : 0.0D;
    }

    public double wallClimbBonus() {
        return this == SILK ? 1.0D : 0.0D;
    }

    public double fleshRegenBonus() {
        return this == FLESH ? 1.0D : 0.0D;
    }

    public double maxManaBonus() {
        return this == MITHRIL_WEAVE ? 20.0D : 0.0D;
    }

    public double spellPowerBonus() {
        return this == MITHRIL_WEAVE ? 0.10D : 0.0D;
    }

    public double cooldownReductionBonus() {
        return this == ARCANE_CLOTH ? 0.02D : 0.0D;
    }

    public double castTimeReductionBonus() {
        return this == ARCANE_CLOTH ? 0.02D : 0.0D;
    }

    public double manaRegenBonus() {
        return this == ARCANE_CLOTH ? 0.03D : 0.0D;
    }

    public double snifferFurSpeedBonus() {
        return this == SNIFFER_FUR ? movementSpeedBonus : 0.0D;
    }

    public double ancientFiberSpeedPenalty() {
        return this == ANCIENT_FIBER ? movementSpeedBonus : 0.0D;
    }

    public static Optional<SockMaterial> byId(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(material -> material.id.equals(normalized)).findFirst();
    }
}
