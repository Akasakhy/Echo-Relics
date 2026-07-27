package dev.kazut.echorelics.echo.shape;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class OrientedSlashShape implements EchoShape {
    private static final double EPSILON = 1.0E-7D;

    private final Vec3 origin;
    private final Vec3 forward;
    private final Vec3 right;
    private final double reach;
    private final double halfWidth;
    private final double minY;
    private final double maxY;
    private final double centerX;
    private final double centerZ;
    private final AABB broadPhaseBounds;

    public OrientedSlashShape(
            Vec3 origin,
            Vec3 horizontalForward,
            double reach,
            double width,
            double minYOffset,
            double maxYOffset) {
        if (reach <= 0.0D || width <= 0.0D || maxYOffset <= minYOffset) {
            throw new IllegalArgumentException("Echo slash dimensions must be positive");
        }

        Vec3 normalized = new Vec3(horizontalForward.x, 0.0D, horizontalForward.z).normalize();
        if (normalized.lengthSqr() < EPSILON) {
            throw new IllegalArgumentException("Echo slash direction must be horizontal and non-zero");
        }

        this.origin = origin;
        this.forward = normalized;
        this.right = new Vec3(-normalized.z, 0.0D, normalized.x);
        this.reach = reach;
        this.halfWidth = width * 0.5D;
        this.minY = origin.y + minYOffset;
        this.maxY = origin.y + maxYOffset;
        this.centerX = origin.x + normalized.x * reach * 0.5D;
        this.centerZ = origin.z + normalized.z * reach * 0.5D;

        double xExtent = Math.abs(forward.x) * reach * 0.5D + Math.abs(right.x) * halfWidth;
        double zExtent = Math.abs(forward.z) * reach * 0.5D + Math.abs(right.z) * halfWidth;
        this.broadPhaseBounds = new AABB(
                centerX - xExtent,
                minY,
                centerZ - zExtent,
                centerX + xExtent,
                maxY,
                centerZ + zExtent);
    }

    @Override
    public Vec3 origin() {
        return origin;
    }

    public Vec3 forward() {
        return forward;
    }

    public double reach() {
        return reach;
    }

    public double halfWidth() {
        return halfWidth;
    }

    @Override
    public AABB broadPhaseBounds() {
        return broadPhaseBounds;
    }

    @Override
    public boolean intersects(AABB target) {
        if (target.maxY < minY || target.minY > maxY) {
            return false;
        }

        double targetCenterX = (target.minX + target.maxX) * 0.5D;
        double targetCenterZ = (target.minZ + target.maxZ) * 0.5D;
        double targetHalfX = (target.maxX - target.minX) * 0.5D;
        double targetHalfZ = (target.maxZ - target.minZ) * 0.5D;
        double deltaX = targetCenterX - centerX;
        double deltaZ = targetCenterZ - centerZ;
        double halfReach = reach * 0.5D;

        if (!overlapsAxis(deltaX, deltaZ, 1.0D, 0.0D,
                halfReach * Math.abs(forward.x) + halfWidth * Math.abs(right.x), targetHalfX)) {
            return false;
        }
        if (!overlapsAxis(deltaX, deltaZ, 0.0D, 1.0D,
                halfReach * Math.abs(forward.z) + halfWidth * Math.abs(right.z), targetHalfZ)) {
            return false;
        }
        if (!overlapsAxis(deltaX, deltaZ, forward.x, forward.z,
                halfReach, targetHalfX * Math.abs(forward.x) + targetHalfZ * Math.abs(forward.z))) {
            return false;
        }
        return overlapsAxis(deltaX, deltaZ, right.x, right.z,
                halfWidth, targetHalfX * Math.abs(right.x) + targetHalfZ * Math.abs(right.z));
    }

    private static boolean overlapsAxis(
            double deltaX,
            double deltaZ,
            double axisX,
            double axisZ,
            double firstRadius,
            double secondRadius) {
        double distance = Math.abs(deltaX * axisX + deltaZ * axisZ);
        return distance <= firstRadius + secondRadius + EPSILON;
    }
}
