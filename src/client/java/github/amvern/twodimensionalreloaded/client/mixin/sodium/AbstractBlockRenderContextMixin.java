package github.amvern.twodimensionalreloaded.client.mixin.sodium;

import github.amvern.twodimensionalreloaded.utils.Plane;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContextMixin {
    @Shadow BlockPos pos;

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private void cullPlane(Direction facing, CallbackInfoReturnable<Boolean> cir) {
        BlockPos pos = this.pos;
        double dist = Plane.sdf(pos.getCenter());

        if (dist <= Plane.getCullDist()) {
            cir.setReturnValue(false);
        } else if (dist <= 0.5) {
            if (facing.getStepY() == 0 && Plane.sdf(pos.relative(facing).getCenter()) <= Plane.getCullDist()) {
                cir.setReturnValue(true);
            }
        }
    }
}