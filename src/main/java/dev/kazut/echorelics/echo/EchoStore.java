package dev.kazut.echorelics.echo;

public interface EchoStore {
    boolean addNew(EchoRecord record, int ownerLimit, int globalLimit);

    EchoRecord peek();

    EchoRecord poll();

    void requeue(EchoRecord record);

    void complete(EchoRecord record);

    void removeActor(EchoActorRef actorRef);

    boolean isEmpty();

    void clear();
}
