package zank.mods.touhou_little_maid_fusion;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import mekanism.common.registries.MekanismDamageTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Handles fusion state logic:
 * - Maid hit by Mek laser → enter fusion state (cancel damage)
 * - While in fusion state → consume hunger
 * - Fusion state visual effects are handled by FusionRenderer (client-side)
 */
@EventBusSubscriber(modid = TouhouLittleMaidFusion.MODID)
public class EventListeners {

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onMaidHurt(MaidAttackEvent event) {
        if (MekanismDamageTypes.LASER.is(event.getSource())) {
            EntityMaid maid = event.getMaid();

            // Cancel the damage
            event.setCanceled(true);

            // Enter fusion state
            FusionState fusionState = maid.getData(TouhouLittleMaidFusionRegistries.AttachmentTypes.FUSION_STATE.get());
            fusionState.startFusion(Config.FUSION_DURATION_TICKS.getAsInt());
        }
    }

    @SubscribeEvent
    static void onMaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) {
            return;
        }

        FusionState fusionState = maid.getData(TouhouLittleMaidFusionRegistries.AttachmentTypes.FUSION_STATE.get());
        if (!fusionState.isInFusion()) {
            return;
        }

        // Tick fusion state countdown
        fusionState.tick();

        // Consume hunger while in fusion state (per tick)
        int currentHunger = maid.getHunger();
        int drainPerTick = Math.max(1, Config.FUSION_HUNGER_DRAIN.getAsInt() / 20);
        int newHunger = Math.max(0, currentHunger - drainPerTick);
        maid.setHunger(newHunger);
    }
}
