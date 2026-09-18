package zank.mods.touhou_little_maid_fusion;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data attachment that tracks whether a maid is in fusion state,
 * how long the state lasts.
 */
public class FusionState {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TouhouLittleMaidFusion.MODID);

    public static final Codec<FusionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("inFusion").forGetter(s -> s.inFusion),
            Codec.INT.fieldOf("fusionTicksRemaining").forGetter(s -> s.fusionTicksRemaining)
    ).apply(instance, FusionState::new));

    public static final Supplier<AttachmentType<FusionState>> TYPE =
            ATTACHMENT_TYPES.register("fusion_state",
                    () -> AttachmentType.<FusionState>builder(FusionState::new)
                            .serialize(CODEC)
                            .copyOnDeath()
                            .build()
            );

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
