package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import dev.kazut.echorelics.entity.ArchivistEntity;
import dev.kazut.echorelics.entity.EchoAvatarEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(EchoRelics.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EchoAvatarEntity>> ECHO_AVATAR =
            ENTITY_TYPES.registerEntityType(
                    "echo_avatar",
                    EchoAvatarEntity::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<ArchivistEntity>> ARCHIVIST =
            ENTITY_TYPES.registerEntityType(
                    "archivist",
                    ArchivistEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder.sized(0.75F, 2.2F)
                            .clientTrackingRange(12)
                            .updateInterval(3));

    private ModEntities() {
    }
}
