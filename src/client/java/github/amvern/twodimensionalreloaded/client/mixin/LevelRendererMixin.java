package github.amvern.twodimensionalreloaded.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import github.amvern.twodimensionalreloaded.client.TwoDimensionalReloadedClient;
import github.amvern.twodimensionalreloaded.client.config.ClientConfig;
import github.amvern.twodimensionalreloaded.utils.Plane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;

import static github.amvern.twodimensionalreloaded.util.ModelBlockAlphaRenderer.renderModelWithAlpha;


@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void renderPlacementOutline(
        PoseStack poseStack, VertexConsumer vertexConsumer,
        double cameraX, double cameraY, double cameraZ,
        BlockOutlineRenderState blockOutlineRenderState,
        int color, float alpha,
        CallbackInfo ci
    ) {
        BlockPos targetPos = blockOutlineRenderState.pos();
        Player player = minecraft.player;
        if (player == null) return;

        if (Plane.shouldCull(targetPos) || targetPos.getZ() > 1 || !player.isWithinBlockInteractionRange(targetPos, 1)) {
            ci.cancel();
            return;
        }

        if(TwoDimensionalReloadedClient.CONFIG.renderBlockPlacementGuide) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof BlockItem blockItem)) return;

//          BlockHitResult hitResult = (BlockHitResult) player.pick(5.0D, 0.0F, false);
            BlockHitResult hitResult = (BlockHitResult) ((LocalPlayerAccessor) player).invokeRaycastHitResult(0.0f, player);

            Block block = blockItem.getBlock();
            Level level = player.level();

            BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack, hitResult);
            BlockState stateToPlace = block.getStateForPlacement(context);
            if (stateToPlace == null) stateToPlace = block.defaultBlockState();

            BlockPos clickedPos = hitResult.getBlockPos();
            BlockState clickedState = level.getBlockState(clickedPos);

            BlockPos placePos = clickedState.canBeReplaced(context)
                ? clickedPos
                : clickedPos.relative(hitResult.getDirection());

            VoxelShape shape = stateToPlace.getShape(level, placePos);
            BlockState worldState = level.getBlockState(placePos);
            boolean replaceable = worldState.canBeReplaced(new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack, hitResult));
            boolean canSurvive = stateToPlace.canSurvive(level, placePos);
            AABB blockBox = stateToPlace.getShape(level, placePos).bounds();
            boolean collidesWithEntity = !level.getEntities(null, blockBox.move(placePos.getX(), placePos.getY(), placePos.getZ())).isEmpty();

            boolean placeable = replaceable && canSurvive && !collidesWithEntity;

            int outlineColor = placeable ? TwoDimensionalReloadedClient.CONFIG.placeableOutlineColor : TwoDimensionalReloadedClient.CONFIG.nonPlaceableOutlineColor;

            if(TwoDimensionalReloadedClient.CONFIG.renderPlacementOutline) {
                ShapeRenderer.renderShape(
                        poseStack,
                        vertexConsumer,
                        shape,
                        placePos.getX() - cameraX,
                        placePos.getY() - cameraY,
                        placePos.getZ() - cameraZ,
                        outlineColor,
                        alpha
                );
            }

            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            VertexConsumer vc = bufferSource.getBuffer(Sheets.translucentBlockItemSheet());
            BlockRenderDispatcher blockRenderDispatcher = minecraft.getBlockRenderer();
            BlockStateModel model = blockRenderDispatcher.getBlockModel(stateToPlace);

            float r = placeable ? 0.0f : 1.0f;
            float g = placeable ? 1.0f : 0.0f;
            float b = 0.0f;
            float a = 0.5f;

            poseStack.pushPose();
            poseStack.translate(placePos.getX() - cameraX, placePos.getY() - cameraY, placePos.getZ() - cameraZ);

            if(TwoDimensionalReloadedClient.CONFIG.blockRenderMode == ClientConfig.RenderStyle.FULL_BLOCK) {
                ModelBlockRenderer.renderModel(poseStack.last(), vc, model, r, g, b, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            } else {
                renderModelWithAlpha(poseStack.last(), vc, model, r, g, b, a, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }

            poseStack.popPose();
        }
       // ci.cancel();
    }
}