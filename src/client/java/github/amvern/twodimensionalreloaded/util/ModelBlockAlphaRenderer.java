package github.amvern.twodimensionalreloaded.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.List;

import static net.minecraft.world.level.SignalGetter.DIRECTIONS;

public class ModelBlockAlphaRenderer {
    public static void renderModelWithAlpha(PoseStack.Pose pose, VertexConsumer vertexConsumer, BlockStateModel blockStateModel, float r, float g, float b, float a, int lightCoords, int overlayCoords) {
        for (BlockModelPart blockModelPart : blockStateModel.collectParts(RandomSource.create(42L))) {
            for (Direction direction : DIRECTIONS) {
                renderQuadListWithAlpha(pose, vertexConsumer, r, g, b, a, blockModelPart.getQuads(direction), lightCoords, overlayCoords);
            }
            renderQuadListWithAlpha(pose, vertexConsumer, r, g, b, a,blockModelPart.getQuads(null), lightCoords, overlayCoords);
        }
    }

    private static void renderQuadListWithAlpha(PoseStack.Pose pose, VertexConsumer vertexConsumer, float r, float g, float b, float a, List<BakedQuad> list, int lightCoords, int overlayCoords) {
        for (BakedQuad bakedQuad : list) {
                float k = Mth.clamp(r, 0.0F, 1.0F);
                float l = Mth.clamp(g, 0.0F, 1.0F);
                float m = Mth.clamp(b, 0.0F, 1.0F);
            vertexConsumer.putBulkData(pose, bakedQuad, k, l, m, a, lightCoords, overlayCoords);
        }
    }
}