package dev.kazut.echorelics.block;

import dev.kazut.echorelics.echo.EchoExecutionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface EchoReactiveBlock {
    boolean onEchoHit(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            EchoExecutionContext context);
}
