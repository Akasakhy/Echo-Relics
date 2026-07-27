package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ModLootTables {
    public static final ResourceKey<LootTable> ARCHIVE_ENTRANCE =
            key("chests/archive_entrance");
    public static final ResourceKey<LootTable> ARCHIVE_SIGIL =
            key("chests/archive_sigil");
    public static final ResourceKey<LootTable> ARCHIVE_REWARD =
            key("chests/archive_reward");

    private ModLootTables() {
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, path));
    }
}
