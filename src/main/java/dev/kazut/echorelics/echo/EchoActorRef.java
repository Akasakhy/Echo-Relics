package dev.kazut.echorelics.echo;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EchoActorRef(EchoActorKind kind, UUID id) {
    public EchoActorRef {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
    }

    public static EchoActorRef player(UUID id) {
        return new EchoActorRef(EchoActorKind.PLAYER, id);
    }

    public static EchoActorRef livingEntity(LivingEntity entity) {
        return entity instanceof Player
                ? player(entity.getUUID())
                : new EchoActorRef(EchoActorKind.LIVING_ENTITY, entity.getUUID());
    }

    public static EchoActorRef device(UUID id) {
        return new EchoActorRef(EchoActorKind.DEVICE, id);
    }
}
