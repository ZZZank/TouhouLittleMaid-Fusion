package zank.mods.touhou_little_maid_fusion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data attachment that tracks whether a maid is in fusion state,
 * how long the state lasts.
 */
public class FusionState {
    public static final Codec<FusionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("inFusion").forGetter(s -> s.inFusion),
            Codec.INT.fieldOf("fusionTicksRemaining").forGetter(s -> s.fusionTicksRemaining)
    ).apply(instance, FusionState::new));

    private boolean inFusion = false;
    private int fusionTicksRemaining = 0;

    public FusionState() {
    }

    private FusionState(boolean inFusion, int fusionTicksRemaining) {
        this.inFusion = inFusion;
        this.fusionTicksRemaining = fusionTicksRemaining;
    }

    public boolean isInFusion() {
        return inFusion && fusionTicksRemaining > 0;
    }

    public void startFusion(int durationTicks) {
        this.inFusion = true;
        this.fusionTicksRemaining = durationTicks;
    }

    public void tick() {
        if (inFusion) {
            fusionTicksRemaining--;
            if (fusionTicksRemaining <= 0) {
                inFusion = false;
                fusionTicksRemaining = 0;
            }
        }
    }

    public int getFusionTicksRemaining() {
        return fusionTicksRemaining;
    }
}
