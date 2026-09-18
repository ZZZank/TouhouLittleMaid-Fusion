package zank.mods.touhou_little_maid_fusion.client;

import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.tileentity.RenderEnergyCube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import zank.mods.touhou_little_maid_fusion.util.FusionState;

/**
 * RenderLayer that draws the Mekanism fusion reactor interior energy core
 * effect on EntityMaid entities that are in fusion state.
 *
 * @see mekanism.generators.client.render.RenderFusionReactor
 */
public class FusionRenderLayer extends RenderLayer<Mob, BedrockModel<Mob>> {

    private final ModelEnergyCore modelEnergyCore;

    public FusionRenderLayer(RenderLayerParent<Mob, BedrockModel<Mob>> renderer, ModelEnergyCore modelEnergyCore) {
        super(renderer);
        this.modelEnergyCore = modelEnergyCore;
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
        // Only render for EntityMaid in fusion state
        if (!(IMaid.convert(mob) instanceof EntityMaid maid) || !maid.isAlive()) {
            return;
        }

        int fusionState = FusionState.get(maid);
        if (!FusionState.inFusion(fusionState)) {
            return;
        }

        ModelEnergyCore core = modelEnergyCore;

        // Get the buffer for the energy core render type
        VertexConsumer buffer = bufferSource.getBuffer(core.RENDER_TYPE);

        long scaledTemp = (long) (ageInTicks * 0.1);

        poseStack.pushPose();
        // Position at entity center
        poseStack.translate(0, maid.getBbHeight() / 2.0, 0);
        // Scale down to entity size
        float baseScale = 0.04F;
        poseStack.scale(baseScale, baseScale, baseScale);

        // Layer 1: RED - oscillating scale, Y rotation -6°/tick, coreVec rotation 36°/tick
        float scale1 = 1.0F + 0.7F * sinDegrees(3.14F * scaledTemp + 135.0F);
        renderPart(poseStack, buffer, EnumColor.RED, scale1, ageInTicks, -6, -7, 0, 36, core);

        // Layer 2: PINK - oscillating scale, Y rotation 4°/tick, coreVec rotation 36°/tick
        float scale2 = 1.0F + 0.8F * sinDegrees(3L * scaledTemp);
        renderPart(poseStack, buffer, EnumColor.PINK, scale2, ageInTicks, 4, 4, 0, 36, core);

        // Layer 3: ORANGE - oscillating scale, Y rotation 5°/tick, coreVec rotation 106°/tick
        float scale3 = 1.0F - 0.9F * sinDegrees(4L * scaledTemp + 90L);
        renderPart(poseStack, buffer, EnumColor.ORANGE, scale3, ageInTicks, 5, -3, -35, 106, core);

        poseStack.popPose();
    }

    private static float sinDegrees(float degrees) {
        return Mth.sin(degrees % 360.0F * ((float) Math.PI / 180F));
    }

    private static void renderPart(
        PoseStack matrix,
        VertexConsumer buffer,
        EnumColor color,
        float scale,
        float ticks,
        int mult1,
        int mult2,
        int shift1,
        int shift2,
        ModelEnergyCore core
    ) {
        matrix.pushPose();
        matrix.scale(scale, scale, scale);
        matrix.mulPose(Axis.YP.rotationDegrees(ticks * (float) mult1 + (float) shift1));
        matrix.mulPose(RenderEnergyCube.coreVec.rotationDegrees(ticks * (float) mult2 + (float) shift2));
        core.render(matrix, buffer, 0xf000f0, OverlayTexture.NO_OVERLAY, color, 1.0F);
        matrix.popPose();
    }
}
