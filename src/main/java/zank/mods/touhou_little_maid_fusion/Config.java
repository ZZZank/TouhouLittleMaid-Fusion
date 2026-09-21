package zank.mods.touhou_little_maid_fusion;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // === Energy Production ===
    public static final ModConfigSpec.DoubleValue ENERGY_PRODUCTION_MULTIPLIER = BUILDER
            .comment("Base energy production multiplier per tick while maid is in fusion state")
            .defineInRange("energyProductionMultiplier", 10000.0, 0.0, 1_000_000.0);

    public static final ModConfigSpec.DoubleValue FAVORABILITY_ENERGY_MULTIPLIER = BUILDER
            .comment("How much favorability affects energy production (multiplied with favorability/100)")
            .defineInRange("favorabilityEnergyMultiplier", 1.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue RANDOM_PERTURBATION_MULTIPLIER = BUILDER
            .comment("How much the UUID-based random perturbation affects energy production")
            .defineInRange("randomPerturbationMultiplier", 0.2, 0.0, 100.0);

    public static final ModConfigSpec.LongValue ENERGY_BUFFER_CAPACITY = BUILDER
            .comment("Maximum energy the controller can store (in FE)")
            .defineInRange("energyBufferCapacity", 100_000_000L, 1L, Long.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
