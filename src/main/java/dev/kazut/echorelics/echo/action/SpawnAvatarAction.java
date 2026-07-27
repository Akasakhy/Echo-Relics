package dev.kazut.echorelics.echo.action;

import dev.kazut.echorelics.echo.EchoAction;
import dev.kazut.echorelics.echo.EchoEffects;
import dev.kazut.echorelics.echo.EchoExecutionContext;
import dev.kazut.echorelics.entity.EchoAvatarEntity;
import dev.kazut.echorelics.entity.EchoAvatarManager;
import dev.kazut.echorelics.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public final class SpawnAvatarAction implements EchoAction {
    private final Vec3 origin;
    private final float recordedYaw;
    private final int lifetimeTicks;

    public SpawnAvatarAction(Vec3 origin, float recordedYaw, int lifetimeTicks) {
        this.origin = origin;
        this.recordedYaw = recordedYaw;
        this.lifetimeTicks = Math.max(1, lifetimeTicks);
    }

    @Override
    public Vec3 origin() {
        return origin;
    }

    @Override
    public void warn(ServerLevel level) {
        EchoEffects.avatarWarning(level, origin);
    }

    @Override
    public void execute(ServerLevel level, EchoExecutionContext context) {
        ServerPlayer owner = context.playerActor();
        if (owner == null) {
            return;
        }

        EchoAvatarEntity avatar = ModEntities.ECHO_AVATAR.get().create(level, EntitySpawnReason.TRIGGERED);
        if (avatar == null) {
            return;
        }
        avatar.snapTo(origin.x(), origin.y(), origin.z(), recordedYaw, 0.0F);
        avatar.initialize(owner.getUUID(), lifetimeTicks, recordedYaw);
        EchoAvatarManager.replaceActive(level, owner.getUUID(), avatar);
        if (level.addFreshEntity(avatar)) {
            EchoEffects.avatarSpawn(level, origin);
        } else {
            EchoAvatarManager.onAvatarRemoved(level.getServer(), owner.getUUID(), avatar.getUUID());
        }
    }
}
