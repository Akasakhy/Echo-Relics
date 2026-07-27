package dev.kazut.echorelics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class ArchiveDeviceNetwork {
    private static final int DOOR_SEARCH_RADIUS = 8;

    private ArchiveDeviceNetwork() {
    }

    public static void notifyNearbyDoors(ServerLevel level, BlockPos devicePos) {
        for (BlockPos checked : BlockPos.betweenClosed(
                devicePos.offset(-DOOR_SEARCH_RADIUS, -3, -DOOR_SEARCH_RADIUS),
                devicePos.offset(DOOR_SEARCH_RADIUS, 3, DOOR_SEARCH_RADIUS))) {
            if (level.isLoaded(checked)
                    && level.getBlockState(checked).getBlock() instanceof ArchiveDoorBlock door) {
                door.refresh(level, checked.immutable());
            }
        }
    }
}
