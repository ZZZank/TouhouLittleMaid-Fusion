package zank.mods.touhou_little_maid_fusion.client;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;

import mekanism.client.model.ModelEnergyCore;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * RenderLayer that draws the Mekanism fusion reactor interior energy core
 * effect on EntityMaid entities that are in fusion state.
 *
 * @see mekanism.generators.client.render.RenderFusionReactor
 */
public class FusionRenderLayer extends RenderLayer<Mob, BedrockModel<Mob>> {

    private final ModelEnergyCore core;

    public FusionRenderLayer(RenderLayerParent<Mob, BedrockModel<Mob>> renderer, ModelEnergyCore core) {
        super(renderer);
        this.core = core;
    }

    @Override
    public void render(
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource bufferSource,
        int packedLight,
        @NotNull Mob mob,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        FusionRenderImpl.render(core, poseStack, bufferSource, packedLight, mob, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
    }
}
