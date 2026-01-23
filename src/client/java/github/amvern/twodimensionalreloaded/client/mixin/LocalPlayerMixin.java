package github.amvern.twodimensionalreloaded.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import github.amvern.twodimensionalreloaded.client.TwoDimensionalReloadedClient;
import github.amvern.twodimensionalreloaded.client.access.MouseNormalizedGetter;
import github.amvern.twodimensionalreloaded.utils.Plane;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Entity {
    @Shadow public ClientInput input;
    @Shadow @Final protected Minecraft minecraft;

    public LocalPlayerMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean countSidewaysMovementOnPlane(boolean original) {
        double moveX = this.input.getMoveVector().x;
        double mouseX = ((MouseNormalizedGetter) minecraft.mouseHandler).twoDimensional$getNormalizedX();
        return original || moveX * Math.signum(mouseX) >= 0.8;

    }

    @Inject(method = "raycastHitResult", at = @At("RETURN"), cancellable = true)
    private void customRaycast(float partialTicks, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
        if(cir.getReturnValue().getType() == HitResult.Type.ENTITY) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();
        Vec3 camForward = new Vec3(camera.forwardVector()).normalize();
        Vec3 camUp = new Vec3(camera.upVector()).normalize();
        Vec3 camRight = camForward.cross(camUp).normalize();

        MouseNormalizedGetter mouseHandler = (MouseNormalizedGetter) minecraft.mouseHandler;
        double mouseNormX = mouseHandler.twoDimensional$getNormalizedX();
        double mouseNormY = mouseHandler.twoDimensional$getNormalizedY();

        double fovY = Math.toRadians(minecraft.options.fov().get());
        double aspect = (double) minecraft.getWindow().getScreenWidth() / minecraft.getWindow().getScreenHeight();
        double halfFovTan = Math.tan(fovY / 2);
        Vec3 rayDirection = camForward
            .add(camRight.scale(-mouseNormX * halfFovTan * aspect))
            .add(camUp.scale(mouseNormY * halfFovTan))
            .normalize();

        double distanceToPlane = (Plane.getZ() - cameraPos.z) / rayDirection.z;
        Vec3 hitPos = cameraPos.add(rayDirection.scale(distanceToPlane));

        BlockPos targetBlock = new BlockPos(
            (int) Math.floor(hitPos.x),
            (int) Math.floor(hitPos.y),
            (TwoDimensionalReloadedClient.faceAway.isDown() ? 1 : 0)
//            (int) Math.floor(hitPos.z)
        );

        Vec3 targetCenter = Vec3.atCenterOf(targetBlock);
        Vec3 delta = cameraPos.subtract(targetCenter);
        Direction face = (Math.abs(delta.x()) > Math.abs(delta.y())) ?
            delta.x() > 0 ? Direction.EAST : Direction.WEST
            : delta.y() > 0 ? Direction.UP : Direction.DOWN;

        BlockHitResult fakeBlockHit = new BlockHitResult(hitPos, face, targetBlock , false);

        boolean hasHorizontalNeighbor = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                BlockPos neighbor = targetBlock.offset(dx, dy, 0);
                if (!minecraft.level.getBlockState(neighbor).isAir()) {
                    hasHorizontalNeighbor = true;
                    break;
                }
            }
            if (hasHorizontalNeighbor) break;
        }

        if (hasHorizontalNeighbor) {
            cir.setReturnValue(fakeBlockHit);
        }
    }
}