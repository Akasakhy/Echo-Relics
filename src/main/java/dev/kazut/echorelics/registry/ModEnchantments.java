package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> REVERBERATION = key("reverberation");
    public static final ResourceKey<Enchantment> ACCELERANDO = key("accelerando");

    private ModEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(
                Registries.ENCHANTMENT,
                Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, path));
    }
}
