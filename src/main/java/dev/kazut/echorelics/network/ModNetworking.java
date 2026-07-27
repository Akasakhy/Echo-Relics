package dev.kazut.echorelics.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                EmptySlashPayload.TYPE,
                EmptySlashPayload.STREAM_CODEC,
                EmptySlashPayload::handle);
    }
}
