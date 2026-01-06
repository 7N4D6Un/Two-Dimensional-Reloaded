package github.amvern.twodimensionalreloaded;

import github.amvern.twodimensionalreloaded.access.EntityPlaneGetterSetter;
import github.amvern.twodimensionalreloaded.access.InteractionLayerGetterSetter;
import github.amvern.twodimensionalreloaded.network.InteractionLayerPayload;
import github.amvern.twodimensionalreloaded.utils.Plane;
import github.amvern.twodimensionalreloaded.utils.PlaneAttachment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.logging.Logger;

public class TwoDimensionalReloaded implements ModInitializer {
    public static final String MOD_ID = "twodimensionalreloaded";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static void setPlayerPlane(MinecraftServer server, ServerPlayer player) {
        BlockPos originalPos = player.blockPosition();
        int adjustedY = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, originalPos.getX(), 0);

        final Plane plane = new Plane();

        server.execute(() -> {
            PlaneAttachment.set(player, plane);
            ((EntityPlaneGetterSetter) player).twoDimensional$setPlane(plane);

            player.setPosRaw(originalPos.getX() + 0.5, adjustedY, 0.5);
        });
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(
                InteractionLayerPayload.TYPE,
                InteractionLayerPayload.CODEC
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            setPlayerPlane(server, handler.getPlayer());
        });

        ServerPlayNetworking.registerGlobalReceiver(
                InteractionLayerPayload.TYPE,
                (payload, ctx) -> {
                    try {
                        ctx.server().execute(() -> {
                            ((InteractionLayerGetterSetter) ctx.player()).setInteractionLayer(payload.mode());
                        });
                    } catch (Exception err) {
                        LOGGER.info(err.getMessage());
                    }
                }
        );
    }
}