package dev.kazut.echorelics.echo.shape;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface EchoShape {
    Vec3 origin();

    AABB broadPhaseBounds();

    boolean intersects(AABB targetBounds);
}
