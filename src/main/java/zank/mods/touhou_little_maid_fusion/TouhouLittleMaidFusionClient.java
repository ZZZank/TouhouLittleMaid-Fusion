package zank.mods.touhou_little_maid_fusion;

import mekanism.client.ClientRegistrationUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import zank.mods.touhou_little_maid_fusion.huh.GuiMaidFusionController;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TouhouLittleMaidFusion.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TouhouLittleMaidFusion.MODID, value = Dist.CLIENT)
public class TouhouLittleMaidFusionClient {
    public TouhouLittleMaidFusionClient(ModContainer container) {
    }

    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(
            event,
            TouhouLittleMaidFusionRegistries.ContainerTypes.CONTROLLER,
            GuiMaidFusionController::new
        );
    }
}
