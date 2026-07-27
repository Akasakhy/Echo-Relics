package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import dev.kazut.echorelics.worldgen.GrandEchoArchivePiece;
import dev.kazut.echorelics.worldgen.GrandEchoArchiveStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStructures {
    public static final ResourceKey<Structure> GRAND_ECHO_ARCHIVE_KEY =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, "grand_echo_archive"));
    public static final ResourceKey<StructureSet> GRAND_ECHO_ARCHIVES_KEY =
            ResourceKey.create(
                    Registries.STRUCTURE_SET,
                    Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, "grand_echo_archives"));

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_TYPE, EchoRelics.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PIECE, EchoRelics.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<GrandEchoArchiveStructure>>
            GRAND_ECHO_ARCHIVE = STRUCTURE_TYPES.register(
                    "grand_echo_archive",
                    () -> () -> GrandEchoArchiveStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType>
            GRAND_ECHO_ARCHIVE_PIECE = STRUCTURE_PIECE_TYPES.register(
                    "grand_echo_archive",
                    () -> (context, tag) -> new GrandEchoArchivePiece(tag));

    private ModStructures() {
    }
}
