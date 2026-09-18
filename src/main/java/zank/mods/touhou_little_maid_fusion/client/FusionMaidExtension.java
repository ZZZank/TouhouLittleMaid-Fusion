package zank.mods.touhou_little_maid_fusion.client;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import mekanism.client.model.ModelEnergyCore;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
}
