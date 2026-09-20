package zank.mods.touhou_little_maid_fusion.client;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import mekanism.client.model.ModelEnergyCore;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author ZZZank
 */
@LittleMaidExtension
public class FusionMaidExtension implements ILittleMaid {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addAdditionMaidLayer(EntityMaidRenderer renderer, EntityRendererProvider.Context context) {
        renderer.addLayer(new FusionRenderLayer(renderer, new ModelEnergyCore(context.getModelSet())));
    }

    @Override
    public void addAdditionGeckoMaidLayer(GeckoEntityMaidRenderer<? extends Mob> renderer, EntityRendererProvider.Context context) {
        renderer.addGeoLayerRenderer(new GeckoFusionRenderLayer<>(renderer, new ModelEnergyCore(context.getModelSet())));
    }
}
