package dev.kazut.echorelics.echo;

import dev.kazut.echorelics.entity.EchoAvatarManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EchoEvents {
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        EchoSystem.tick(event.getServer());
        EchoAvatarManager.tick(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        removePlayerRecords(event.getEntity() instanceof ServerPlayer player ? player : null);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        removePlayerRecords(event.getEntity() instanceof ServerPlayer player ? player : null);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            removePlayerRecords(event.getEntity() instanceof ServerPlayer player ? player : null);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            removePlayerRecords(player);
        } else {
            EchoSystem.removeLivingEntity(level.getServer(), event.getEntity());
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        EchoSystem.shutdown(event.getServer());
        EchoAvatarManager.shutdown(event.getServer());
    }

    private static void removePlayerRecords(ServerPlayer player) {
        if (player != null && player.level() instanceof ServerLevel level) {
            EchoSystem.removePlayer(level.getServer(), player.getUUID());
            EchoAvatarManager.removeOwner(level.getServer(), player.getUUID());
        }
    }
}
