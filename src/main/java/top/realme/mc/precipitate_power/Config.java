package top.realme.mc.precipitate_power;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue GENERATOR_CAPACITY = BUILDER
            .comment("Internal FE capacity of the precipitate generator.")
            .defineInRange("generatorCapacity", 100000, 1000, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue GENERATOR_MAX_EXTRACT = BUILDER
            .comment("Maximum FE extracted per tick from each horizontal side.")
            .defineInRange("generatorMaxExtract", 20000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue TRAVEL_SOCK_MAX_EXTRACT_BOOST = BUILDER
            .comment("Permanent FE/t max output boost added to a generator when a travel disposable sock breaks inside it.")
            .defineInRange("travelSockMaxExtractBoost", 100, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue TRAVEL_SOCK_GENERATION_MULTIPLIER = BUILDER
            .comment("Generation multiplier used while a travel disposable sock is generating.")
            .defineInRange("travelSockGenerationMultiplier", 2.0D, 0.0D, 1000.0D);

    public static final ModConfigSpec.IntValue ADVANCED_GENERATOR_WATER_CAPACITY = BUILDER
            .comment("Internal water tank capacity of the advanced precipitate generator in mB.")
            .defineInRange("advancedGeneratorWaterCapacity", 8000, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ADVANCED_GENERATOR_BASE_WATER_USE = BUILDER
            .comment("Base water consumption per generation tick for the advanced precipitate generator in mB.")
            .defineInRange("advancedGeneratorBaseWaterUse", 10, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ADVANCED_GENERATOR_WATER_USE_PER_PRECIPITATION = BUILDER
            .comment("Additional water consumed per precipitation level for the advanced precipitate generator in mB.")
            .defineInRange("advancedGeneratorWaterUsePerPrecipitation", 5, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue PRECIPITATE_CHANCE = BUILDER
            .comment("Chance checked every 20 ticks for a white sock to precipitate.")
            .defineInRange("precipitateChance", 0.015D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue DIRTY_BASE_CHANCE = BUILDER
            .comment("Base per-tick chance for a sock to gain one dirt stain.")
            .defineInRange("dirtyBaseChance", 0.0012D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue DIRTY_CHANCE_PER_PRECIPITATION = BUILDER
            .comment("Additional per-tick dirty chance for each precipitation level.")
            .defineInRange("dirtyChancePerPrecipitation", 0.00045D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue SOCK_STAIN_LIMIT = BUILDER
            .comment("How many stains a white sock can accumulate before becoming a dirty white sock.")
            .defineInRange("sockStainLimit", 10, 1, 999);

    public static final ModConfigSpec.DoubleValue LOOT_SOCK_CHANCE = BUILDER
            .comment("Chance for chest loot generation to add a white sock.")
            .defineInRange("lootSockChance", 0.09D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue LOOT_BONUS_CHANCE = BUILDER
            .comment("Chance for generated loot socks to receive a power coefficient bonus.")
            .defineInRange("lootBonusChance", 0.35D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue LOOT_MIN_COEFFICIENT = BUILDER
            .comment("Minimum random power coefficient applied to loot socks.")
            .defineInRange("lootMinCoefficient", 1.1D, 1.0D, 1000.0D);

    public static final ModConfigSpec.DoubleValue LOOT_MAX_COEFFICIENT = BUILDER
            .comment("Maximum random power coefficient applied to loot socks.")
            .defineInRange("lootMaxCoefficient", 2.4D, 1.0D, 1000.0D);

    public static final ModConfigSpec.ConfigValue<String> GENERATION_FORMULA = BUILDER
            .comment("Formula for generator FE/t before coefficient multiplier. Variable x = precipitation level. Supports + - * / ^ and parentheses.")
            .define("generationFormula", "12 + 2.5 * x + 0.35 * x ^ 2");

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
