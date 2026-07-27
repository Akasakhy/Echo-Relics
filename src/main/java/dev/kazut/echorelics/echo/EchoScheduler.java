package dev.kazut.echorelics.echo;

import com.mojang.logging.LogUtils;
import dev.kazut.echorelics.config.EchoRelicsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public final class EchoScheduler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final EchoStore store;
    private long tick;
    private long nextSequence;

    public EchoScheduler(EchoStore store) {
        this.store = store;
    }

    public boolean schedule(
            EchoActorRef actorRef,
            EchoProvenance provenance,
            net.minecraft.resources.ResourceKey<Level> dimension,
            EchoAction action,
            int replayCount,
            int intervalTicks,
            int warningLeadTicks) {
        EchoRecord record = new EchoRecord(
                nextSequence++,
                actorRef,
                provenance,
                dimension,
                action,
                replayCount,
                intervalTicks,
                warningLeadTicks,
                tick);
        return store.addNew(
                record,
                EchoRelicsConfig.MAX_PENDING_PER_OWNER.getAsInt(),
                EchoRelicsConfig.MAX_PENDING_GLOBAL.getAsInt());
    }

    public void tick(MinecraftServer server) {
        tick++;
        if (store.isEmpty()) {
            return;
        }

        int replayLimit = EchoRelicsConfig.MAX_REPLAYS_PER_TICK.getAsInt();
        int replayCount = 0;
        int eventBudget = Math.max(256, replayLimit * 2);
        int processedEvents = 0;

        while (processedEvents < eventBudget) {
            EchoRecord record = store.peek();
            if (record == null || record.nextEventTick() > tick) {
                break;
            }
            if (!record.warningPending() && replayCount >= replayLimit) {
                break;
            }

            store.poll();
            processedEvents++;

            ServerLevel level = server.getLevel(record.dimension());
            if (level == null) {
                store.complete(record);
                continue;
            }

            EchoExecutionContext context = resolveContext(server, level, record);
            if (context == null) {
                store.complete(record);
                continue;
            }

            if (record.warningPending()) {
                try {
                    record.action().warn(level);
                    record.markWarningEmitted();
                    store.requeue(record);
                } catch (RuntimeException exception) {
                    failRecord(record, "warning", exception);
                }
                continue;
            }

            replayCount++;
            try {
                ChunkPos chunk = ChunkPos.containing(BlockPos.containing(record.action().origin()));
                if (level.getChunkSource().getChunkNow(chunk.x(), chunk.z()) != null) {
                    record.action().execute(level, context);
                }
            } catch (RuntimeException exception) {
                failRecord(record, "replay", exception);
                continue;
            }

            if (record.finishReplayAndAdvance()) {
                store.requeue(record);
            } else {
                store.complete(record);
            }
        }
    }

    public void removeActor(EchoActorRef actorRef) {
        store.removeActor(actorRef);
    }

    public void clear() {
        store.clear();
    }

    private static EchoExecutionContext resolveContext(
            MinecraftServer server,
            ServerLevel level,
            EchoRecord record) {
        LivingEntity actor;
        switch (record.actorRef().kind()) {
            case PLAYER -> {
                ServerPlayer player = server.getPlayerList().getPlayer(record.actorRef().id());
                if (player == null || player.level() != level) {
                    return null;
                }
                actor = player;
            }
            case LIVING_ENTITY -> {
                if (!(level.getEntity(record.actorRef().id()) instanceof LivingEntity living)) {
                    return null;
                }
                actor = living;
            }
            case DEVICE -> {
                return new EchoExecutionContext(record.actorRef(), record.provenance(), null);
            }
            default -> throw new IllegalStateException("Unhandled echo actor kind: " + record.actorRef().kind());
        }

        if (!actor.isAlive() || actor.isRemoved() || actor.isSpectator()) {
            return null;
        }
        return new EchoExecutionContext(record.actorRef(), record.provenance(), actor);
    }

    private void failRecord(EchoRecord record, String phase, RuntimeException exception) {
        LOGGER.error(
                "Discarding echo record {} for actor {} after a {} failure",
                record.sequence(),
                record.actorRef(),
                phase,
                exception);
        store.complete(record);
    }
}
