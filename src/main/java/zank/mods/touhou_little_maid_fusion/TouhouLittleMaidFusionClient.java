package zank.mods.touhou_little_maid_fusion;

import mekanism.client.ClientRegistrationUtil;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import zank.mods.touhou_little_maid_fusion.huh.GuiMaidFusionController;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TouhouLittleMaidFusion.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TouhouLittleMaidFusion.MODID, value = Dist.CLIENT)
public class TouhouLittleMaidFusionClient {
    public TouhouLittleMaidFusionClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(
            event,
            TouhouLittleMaidFusionRegistries.ContainerTypes.CONTROLLER,
            GuiMaidFusionController::new
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
            TouhouLittleMaidFusionRegistries.EntityTypes.HAVE_A_SEAT_PLS.get(),
            NoopRenderer::new
        );
    }
}
