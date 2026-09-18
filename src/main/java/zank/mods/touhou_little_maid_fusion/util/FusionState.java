package zank.mods.touhou_little_maid_fusion.util;

import net.neoforged.neoforge.attachment.IAttachmentHolder;
import zank.mods.touhou_little_maid_fusion.TouhouLittleMaidFusionRegistries;

/**
 * @author ZZZank
 */
public interface FusionState {

    static int get(IAttachmentHolder holder) {
        return holder.getData(TouhouLittleMaidFusionRegistries.AttachmentTypes.FUSION_STATE.get());
    }

    static void set(IAttachmentHolder holder, int fusionState) {
        holder.setData(TouhouLittleMaidFusionRegistries.AttachmentTypes.FUSION_STATE.get(), fusionState);
    }

    static boolean inFusion(int fusionState) {
        return fusionState > 0;
    }
}
