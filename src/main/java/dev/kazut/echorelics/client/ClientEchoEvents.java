package dev.kazut.echorelics.client;

import dev.kazut.echorelics.network.EmptySlashPayload;
import dev.kazut.echorelics.item.EchoBladeItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ClientEchoEvents {
    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (EchoBladeItem.isEchoBlade(event.getItemStack())) {
            ClientPacketDistributor.sendToServer(EmptySlashPayload.INSTANCE);
        }
    }
}
