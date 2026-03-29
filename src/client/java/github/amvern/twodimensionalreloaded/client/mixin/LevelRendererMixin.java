package github.amvern.twodimensionalreloaded.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import github.amvern.twodimensionalreloaded.utils.Plane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void renderPlacementOutline(
            PoseStack poseStack, VertexConsumer builder, double camX, double camY, double camZ, net.minecraft.client.renderer.state.level.BlockOutlineRenderState state, int color, float width, CallbackInfo ci
    ) {
        BlockPos targetPos = state.pos();
        Player player = minecraft.player;
        if (player == null) return;

        if (Plane.shouldCull(targetPos) || targetPos.getZ() > 1 || !player.isWithinBlockInteractionRange(targetPos, 1)) {
            ci.cancel();
        }

       // ci.cancel();
    }
}