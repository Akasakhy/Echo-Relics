package dev.kazut.echorelics.echo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface EchoAction {
    Vec3 origin();

    void warn(ServerLevel level);

    void execute(ServerLevel level, EchoExecutionContext context);
}
