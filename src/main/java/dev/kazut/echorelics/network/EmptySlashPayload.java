package dev.kazut.echorelics.network;

import dev.kazut.echorelics.EchoRelics;
import dev.kazut.echorelics.item.capture.EchoBladeCapture;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EmptySlashPayload() implements CustomPacketPayload {
    public static final EmptySlashPayload INSTANCE = new EmptySlashPayload();
    public static final Type<EmptySlashPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, "empty_slash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EmptySlashPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<EmptySlashPayload> type() {
        return TYPE;
    }

    public static void handle(EmptySlashPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            EchoBladeCapture.captureEmptySwing(player);
        }
    }
}
