package dev.kazut.echorelics.echo;

import dev.kazut.echorelics.block.EchoReactiveBlock;
import dev.kazut.echorelics.config.EchoRelicsConfig;
import dev.kazut.echorelics.echo.shape.OrientedSlashShape;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class EchoBlockInteraction {
    private EchoBlockInteraction() {
    }

    public static int triggerSlash(
            ServerLevel level,
            OrientedSlashShape shape,
            EchoExecutionContext context) {
        AABB bounds = shape.broadPhaseBounds();
        int minX = (int) Math.floor(bounds.minX);
        int minY = (int) Math.floor(bounds.minY);
        int minZ = (int) Math.floor(bounds.minZ);
        int maxX = (int) Math.floor(bounds.maxX);
        int maxY = (int) Math.floor(bounds.maxY);
        int maxZ = (int) Math.floor(bounds.maxZ);
        int maxChecked = EchoRelicsConfig.MAX_REACTIVE_BLOCKS_PER_REPLAY.getAsInt();
        int checked = 0;
        int activated = 0;

        search:
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (checked++ >= maxChecked) {
                        break search;
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof EchoReactiveBlock reactive
                            && shape.intersects(new AABB(pos))
                            && reactive.onEchoHit(level, pos, state, context)) {
                        activated++;
                    }
                }
            }
        }
        return activated;
    }
}
