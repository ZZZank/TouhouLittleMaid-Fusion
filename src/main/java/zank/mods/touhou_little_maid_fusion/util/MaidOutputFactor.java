package zank.mods.touhou_little_maid_fusion.util;

import zank.mods.touhou_little_maid_fusion.Config;

import java.util.UUID;

public interface MaidOutputFactor {

    /**
     * Energy production factor contributed by favorability: {@code 1 + favorability / 384 * multiplier}.
     * May be less than 1.0 when favorability is negative.
     * Shared by server-side energy production and client-side GUI display.
     */
    static double favorability(int favorability) {
        return 1.0 + (favorability / 100.0) * Config.FAVORABILITY_ENERGY_MULTIPLIER.getAsDouble();
    }

    /**
     * Deterministic production variation factor of a maid derived from her UUID,
     * in range {@code [1.0, 1.0 + perMaidProductionVariation]}.
     * Shared by server-side energy production and client-side GUI display.
     */
    static double variation(UUID maidUUID) {
        return 1.0 + (Math.sin(maidUUID.hashCode()) * 0.5 + 0.5) * Config.PER_MAID_PRODUCTION_VARIATION.getAsDouble();
    }
}
