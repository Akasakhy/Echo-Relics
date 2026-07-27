package dev.kazut.echorelics.entity;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class EchoAvatarManager {
    private static final Map<MinecraftServer, Map<UUID, ActiveAvatar>> ACTIVE = new IdentityHashMap<>();

    private EchoAvatarManager() {
    }

    public static void replaceActive(ServerLevel level, UUID ownerId, EchoAvatarEntity avatar) {
        removeOwner(level.getServer(), ownerId);
        ACTIVE.computeIfAbsent(level.getServer(), ignored -> new HashMap<>())
                .put(ownerId, new ActiveAvatar(avatar, avatar.expiryGameTime()));
    }

    public static void removeOwner(MinecraftServer server, UUID ownerId) {
        Map<UUID, ActiveAvatar> serverAvatars = ACTIVE.get(server);
        if (serverAvatars == null) {
            return;
        }

        ActiveAvatar active = serverAvatars.remove(ownerId);
        if (active != null) {
            active.entity().discard();
        }
        if (serverAvatars.isEmpty()) {
            ACTIVE.remove(server);
        }
    }

    public static void onAvatarRemoved(MinecraftServer server, UUID ownerId, UUID avatarId) {
        Map<UUID, ActiveAvatar> serverAvatars = ACTIVE.get(server);
        ActiveAvatar active = serverAvatars == null ? null : serverAvatars.get(ownerId);
        if (active == null || !avatarId.equals(active.entity().getUUID())) {
            return;
        }
        serverAvatars.remove(ownerId);
        if (serverAvatars.isEmpty()) {
            ACTIVE.remove(server);
        }
    }

    public static void tick(MinecraftServer server) {
        Map<UUID, ActiveAvatar> serverAvatars = ACTIVE.get(server);
        if (serverAvatars == null) {
            return;
        }

        Iterator<ActiveAvatar> iterator = serverAvatars.values().iterator();
        while (iterator.hasNext()) {
            ActiveAvatar active = iterator.next();
            EchoAvatarEntity avatar = active.entity();
            if (avatar.isRemoved()) {
                iterator.remove();
            } else if (avatar.level() instanceof ServerLevel level
                    && level.getGameTime() >= active.expiryGameTime()) {
                iterator.remove();
                avatar.expire();
            }
        }
        if (serverAvatars.isEmpty()) {
            ACTIVE.remove(server);
        }
    }

    public static void shutdown(MinecraftServer server) {
        ACTIVE.remove(server);
    }

    private record ActiveAvatar(EchoAvatarEntity entity, long expiryGameTime) {
    }
}
