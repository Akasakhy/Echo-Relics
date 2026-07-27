package dev.kazut.echorelics.echo;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class EchoSystem {
    private static final Map<MinecraftServer, EchoScheduler> SCHEDULERS = new IdentityHashMap<>();

    private EchoSystem() {
    }

    public static boolean schedule(
            ServerLevel level,
            EchoActorRef actorRef,
            EchoProvenance provenance,
            EchoAction action,
            int replayCount,
            int intervalTicks,
            int warningLeadTicks) {
        EchoScheduler scheduler = SCHEDULERS.computeIfAbsent(
                level.getServer(),
                ignored -> new EchoScheduler(new TransientEchoStore()));
        return scheduler.schedule(
                actorRef,
                provenance,
                level.dimension(),
                action,
                replayCount,
                intervalTicks,
                warningLeadTicks);
    }

    public static void tick(MinecraftServer server) {
        EchoScheduler scheduler = SCHEDULERS.get(server);
        if (scheduler != null) {
            scheduler.tick(server);
        }
    }

    public static boolean schedulePlayer(
            ServerLevel level,
            UUID playerId,
            EchoAction action,
            int replayCount,
            int intervalTicks,
            int warningLeadTicks) {
        return schedule(
                level,
                EchoActorRef.player(playerId),
                EchoProvenance.PLAYER_RECORDED,
                action,
                replayCount,
                intervalTicks,
                warningLeadTicks);
    }

    public static void removeActor(MinecraftServer server, EchoActorRef actorRef) {
        EchoScheduler scheduler = SCHEDULERS.get(server);
        if (scheduler != null) {
            scheduler.removeActor(actorRef);
        }
    }

    public static void removePlayer(MinecraftServer server, UUID playerId) {
        removeActor(server, EchoActorRef.player(playerId));
    }

    public static void removeLivingEntity(MinecraftServer server, net.minecraft.world.entity.LivingEntity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            removeActor(server, EchoActorRef.livingEntity(entity));
        }
    }

    public static void shutdown(MinecraftServer server) {
        EchoScheduler scheduler = SCHEDULERS.remove(server);
        if (scheduler != null) {
            scheduler.clear();
        }
    }
}
