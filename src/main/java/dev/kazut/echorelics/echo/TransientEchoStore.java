package dev.kazut.echorelics.echo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public final class TransientEchoStore implements EchoStore {
    private final PriorityQueue<EchoRecord> queue = new PriorityQueue<>(
            Comparator.comparingLong(EchoRecord::nextEventTick)
                    .thenComparingLong(EchoRecord::sequence));
    private final Map<EchoActorRef, Integer> actorCounts = new HashMap<>();
    private int totalCount;

    @Override
    public boolean addNew(EchoRecord record, int ownerLimit, int globalLimit) {
        int actorCount = actorCounts.getOrDefault(record.actorRef(), 0);
        if (actorCount >= ownerLimit || totalCount >= globalLimit) {
            return false;
        }

        queue.add(record);
        actorCounts.put(record.actorRef(), actorCount + 1);
        totalCount++;
        return true;
    }

    @Override
    public EchoRecord peek() {
        return queue.peek();
    }

    @Override
    public EchoRecord poll() {
        return queue.poll();
    }

    @Override
    public void requeue(EchoRecord record) {
        queue.add(record);
    }

    @Override
    public void complete(EchoRecord record) {
        int nextActorCount = actorCounts.getOrDefault(record.actorRef(), 0) - 1;
        if (nextActorCount <= 0) {
            actorCounts.remove(record.actorRef());
        } else {
            actorCounts.put(record.actorRef(), nextActorCount);
        }
        totalCount = Math.max(0, totalCount - 1);
    }

    @Override
    public void removeActor(EchoActorRef actorRef) {
        int removed = actorCounts.getOrDefault(actorRef, 0);
        if (removed == 0) {
            return;
        }

        queue.removeIf(record -> record.actorRef().equals(actorRef));
        actorCounts.remove(actorRef);
        totalCount = Math.max(0, totalCount - removed);
    }

    @Override
    public boolean isEmpty() {
        return totalCount == 0;
    }

    @Override
    public void clear() {
        queue.clear();
        actorCounts.clear();
        totalCount = 0;
    }
}
