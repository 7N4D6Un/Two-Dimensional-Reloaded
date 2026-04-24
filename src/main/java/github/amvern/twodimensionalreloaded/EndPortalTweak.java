package github.amvern.twodimensionalreloaded;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.util.HashSet;
import java.util.Set;

/**
 * A temporary method for activating the End Portal that hasn't been thoroughly tested.
 * Simply drop enough Ender Eyes near the End Portal frame.
 */
public class EndPortalTweak {
	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerLevel world) -> {
			if (!(entity instanceof ItemEntity itemEntity)) return;
			if (!itemEntity.getItem().is(Items.ENDER_EYE)) return;
			BlockPos eyePos = itemEntity.blockPosition();

			AABB searchBox = new AABB(
				itemEntity.getX() - 7, itemEntity.getY() - 5, itemEntity.getZ() - 7,
				itemEntity.getX() + 7, itemEntity.getY() + 5, itemEntity.getZ() + 7
			);

			BlockPos closestEmptyFrame = null;
			double closestEmptyDist = Double.MAX_VALUE;

			int filledCount = 0;
			int totalFrameCount = 0;
			Set<BlockPos> filledFrames = new HashSet<>();

			for (BlockPos pos : BlockPos.betweenClosed(
				(int) searchBox.minX, (int) searchBox.minY, (int) searchBox.minZ,
				(int) searchBox.maxX, (int) searchBox.maxY, (int) searchBox.maxZ)) {

				BlockState state = world.getBlockState(pos);

				if (state.is(Blocks.END_PORTAL)) {
					return;
				}

				if (!state.is(Blocks.END_PORTAL_FRAME)) continue;

				totalFrameCount++;
				if (state.getValue(EndPortalFrameBlock.HAS_EYE)) {
					filledCount++;
					filledFrames.add(pos.immutable());
				} else {
					double dist = eyePos.distSqr(pos);
					if (dist < closestEmptyDist) {
						closestEmptyDist = dist;
						closestEmptyFrame = pos.immutable();
					}
				}
			}

			if (closestEmptyFrame != null) {
				BlockState activatedState = world.getBlockState(closestEmptyFrame)
					.setValue(EndPortalFrameBlock.HAS_EYE, true);
				world.setBlock(closestEmptyFrame, activatedState, 3);
				world.playSound(null, closestEmptyFrame, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				itemEntity.discard();

				if (filledCount + 1 >= totalFrameCount && totalFrameCount > 0) {
					generateEndPortal(world, filledFrames);
				}
				return;
			}

			if (filledCount > 0) {
				boolean portalGenerated = generateEndPortal(world, filledFrames);

				if (portalGenerated) {
					itemEntity.discard();
				}
			}
		});
	}
	
	private static boolean generateEndPortal(ServerLevel world, Set<BlockPos> filledFrames) {
		Set<Long> processedPairs = new HashSet<>();
		boolean anyPortalPlaced = false;
		int portalCenterX = 0, portalCenterY = 0, portalCenterZ = 0;
		int portalBlockCount = 0;

		for (BlockPos framePos : filledFrames) {
			int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
			for (int[] dir : directions) {
				BlockPos checkPos = framePos.offset(dir[0] * 4, 0, dir[1] * 4);

				if (filledFrames.contains(checkPos)) {
					long pairKey;
					if (framePos.getX() < checkPos.getX() ||
						(framePos.getX() == checkPos.getX() && framePos.getZ() < checkPos.getZ())) {
						pairKey = ((long) framePos.getX() << 48) | ((long) framePos.getZ() << 32) |
								((long) checkPos.getX() << 16) | (checkPos.getZ() & 0xFFFF);
					} else {
						pairKey = ((long) checkPos.getX() << 48) | ((long) checkPos.getZ() << 32) |
								((long) framePos.getX() << 16) | (framePos.getZ() & 0xFFFF);
					}

					if (processedPairs.contains(pairKey)) continue;
					processedPairs.add(pairKey);

					for (int i = 1; i <= 3; i++) {
						BlockPos portalPos = framePos.offset(dir[0] * i, 0, dir[1] * i);
						BlockState existing = world.getBlockState(portalPos);
						if (existing.isAir() || !existing.isSolid()) {
							world.setBlock(portalPos, Blocks.END_PORTAL.defaultBlockState(), 3);
							anyPortalPlaced = true;
							portalCenterX += portalPos.getX();
							portalCenterY += portalPos.getY();
							portalCenterZ += portalPos.getZ();
							portalBlockCount++;
						}
					}
				}
			}
		}

		if (anyPortalPlaced && portalBlockCount > 0) {
			double centerX = (double) portalCenterX / portalBlockCount;
			double centerY = (double) portalCenterY / portalBlockCount;
			double centerZ = (double) portalCenterZ / portalBlockCount;
			world.playSound(null, centerX, centerY, centerZ,
				SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		return anyPortalPlaced;
	}
}