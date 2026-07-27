package dev.kazut.echorelics.echo;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class EchoRecord {
    private final long sequence;
    private final EchoActorRef actorRef;
    private final EchoProvenance provenance;
    private final ResourceKey<Level> dimension;
    private final EchoAction action;
    private final int intervalTicks;
    private final int warningLeadTicks;
    private int remainingReplays;
    private long dueTick;
    private boolean warningEmitted;

    public EchoRecord(
            long sequence,
            EchoActorRef actorRef,
            EchoProvenance provenance,
            ResourceKey<Level> dimension,
            EchoAction action,
            int replayCount,
            int intervalTicks,
            int warningLeadTicks,
            long currentTick) {
        this.sequence = sequence;
        this.actorRef = actorRef;
        this.provenance = provenance;
        this.dimension = dimension;
        this.action = action;
        this.remainingReplays = replayCount;
        this.intervalTicks = intervalTicks;
        this.warningLeadTicks = warningLeadTicks;
        // Records are created during a server tick and consumed from its Post event.
        // The extra tick keeps an interval of 60 from firing at the end of tick 59.
        this.dueTick = currentTick + intervalTicks + 1L;
    }

    public long sequence() {
        return sequence;
    }

    public EchoActorRef actorRef() {
        return actorRef;
    }

    public EchoProvenance provenance() {
        return provenance;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public EchoAction action() {
        return action;
    }

    public long nextEventTick() {
        return warningEmitted ? dueTick : Math.max(0L, dueTick - warningLeadTicks);
    }

    public boolean warningPending() {
        return !warningEmitted;
    }

    public void markWarningEmitted() {
        warningEmitted = true;
    }

    public boolean finishReplayAndAdvance() {
        remainingReplays--;
        if (remainingReplays <= 0) {
            return false;
        }

        dueTick += intervalTicks;
        warningEmitted = false;
        return true;
    }
}
