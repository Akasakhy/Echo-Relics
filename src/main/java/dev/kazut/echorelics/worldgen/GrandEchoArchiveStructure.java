package dev.kazut.echorelics.worldgen;

import com.mojang.serialization.MapCodec;
import dev.kazut.echorelics.registry.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class GrandEchoArchiveStructure extends Structure {
    private static final int MAX_TERRAIN_VARIATION = 4;

    public static final MapCodec<GrandEchoArchiveStructure> CODEC =
            simpleCodec(GrandEchoArchiveStructure::new);

    public GrandEchoArchiveStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int minX = context.chunkPos().getMiddleBlockX() - GrandEchoArchivePiece.WIDTH / 2;
        int minZ = context.chunkPos().getMiddleBlockZ() - GrandEchoArchivePiece.DEPTH / 2;
        int[] heights = sampleTerrain(context, minX, minZ);
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        int total = 0;
        for (int height : heights) {
            lowest = Math.min(lowest, height);
            highest = Math.max(highest, height);
            total += height;
        }
        if (highest - lowest > MAX_TERRAIN_VARIATION) {
            return Optional.empty();
        }
        int surfaceY = total / heights.length;
        BlockPos pieceOrigin = new BlockPos(minX, surfaceY - 1, minZ);
        BlockPos locator = new BlockPos(
                context.chunkPos().getMiddleBlockX(),
                surfaceY,
                context.chunkPos().getMiddleBlockZ());
        return Optional.of(new GenerationStub(
                locator,
                builder -> builder.addPiece(new GrandEchoArchivePiece(pieceOrigin))));
    }

    private static int[] sampleTerrain(GenerationContext context, int minX, int minZ) {
        int middleX = minX + GrandEchoArchivePiece.WIDTH / 2;
        int maxX = minX + GrandEchoArchivePiece.WIDTH - 1;
        int middleZ = minZ + GrandEchoArchivePiece.DEPTH / 2;
        int maxZ = minZ + GrandEchoArchivePiece.DEPTH - 1;
        int[] sampleX = {minX, middleX, maxX};
        int[] sampleZ = {minZ, middleZ, maxZ};
        int[] heights = new int[sampleX.length * sampleZ.length];
        int index = 0;
        for (int x : sampleX) {
            for (int z : sampleZ) {
                heights[index++] = context.chunkGenerator().getFirstOccupiedHeight(
                        x,
                        z,
                        Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(),
                        context.randomState());
            }
        }
        return heights;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.GRAND_ECHO_ARCHIVE.get();
    }
}
