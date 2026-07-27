package dev.kazut.echorelics.echo;

import org.jspecify.annotations.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public record EchoExecutionContext(
        EchoActorRef actorRef,
        EchoProvenance provenance,
        @Nullable LivingEntity actor) {

    public @Nullable ServerPlayer playerActor() {
        return actor instanceof ServerPlayer player ? player : null;
    }
}
