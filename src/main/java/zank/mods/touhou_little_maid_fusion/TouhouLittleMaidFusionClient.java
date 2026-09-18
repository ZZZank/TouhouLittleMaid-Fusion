package zank.mods.touhou_little_maid_fusion;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;

import mekanism.client.ClientRegistrationUtil;
import mekanism.client.model.ModelEnergyCore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import zank.mods.touhou_little_maid_fusion.client.FusionRenderLayer;
import zank.mods.touhou_little_maid_fusion.huh.GuiMaidFusionController;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TouhouLittleMaidFusion.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TouhouLittleMaidFusion.MODID, value = Dist.CLIENT)
public class TouhouLittleMaidFusionClient {
    public TouhouLittleMaidFusionClient(ModContainer container) {
    }

    @SubscribeEvent
    public static void addFusionLayer(EntityRenderersEvent.AddLayers event) {
        for (var entityType : event.getEntityTypes()) {
            var renderer = event.getRenderer(entityType);
            if (renderer instanceof EntityMaidRenderer maidRenderer) {
                maidRenderer.addLayer(new FusionRenderLayer(maidRenderer, new ModelEnergyCore(event.getEntityModels())));
            }
        }
    }

    public static void registerScreen(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(
            event,
            TouhouLittleMaidFusionRegistries.ContainerType.CONTROLLER,
            GuiMaidFusionController::new
        );
    }
}
