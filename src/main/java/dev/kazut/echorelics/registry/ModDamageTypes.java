package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> ECHO_SLASH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, "echo_slash"));

    private ModDamageTypes() {
    }
}
