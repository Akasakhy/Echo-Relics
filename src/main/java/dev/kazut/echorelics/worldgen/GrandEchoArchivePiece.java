package dev.kazut.echorelics.worldgen;

import dev.kazut.echorelics.block.ArchiveDoorBlock;
import dev.kazut.echorelics.entity.ArchivistEntity;
import dev.kazut.echorelics.registry.ModBlocks;
import dev.kazut.echorelics.registry.ModEntities;
import dev.kazut.echorelics.registry.ModLootTables;
import dev.kazut.echorelics.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class GrandEchoArchivePiece extends StructurePiece {
    public static final int WIDTH = 17;
    public static final int HEIGHT = 9;
    public static final int DEPTH = 109;

    private static final int FIRST_DOOR_Z = 29;
    private static final int SECOND_DOOR_Z = 52;
    private static final int THIRD_DOOR_Z = 80;
    private static final int BOSS_HOME_Z = 92;
    private static final int BOSS_REWARD_GATE_Z = 100;
    private static final int BOSS_EXIT_GATE_Z = 106;
    private static final int[] GUARD_X = {4, 8, 12};
    private static final int[] GUARD_Z = {62, 68, 62};

    private int spawnedGuardMask;
    private boolean spawnedArchivist;

    public GrandEchoArchivePiece(BlockPos minCorner) {
        super(
                ModStructures.GRAND_ECHO_ARCHIVE_PIECE.get(),
                0,
                makeBoundingBox(
                        minCorner.getX(),
                        minCorner.getY(),
                        minCorner.getZ(),
                        Direction.NORTH,
                        WIDTH,
                        HEIGHT,
                        DEPTH));
        setOrientation(Direction.NORTH);
    }

    public GrandEchoArchivePiece(CompoundTag tag) {
        super(ModStructures.GRAND_ECHO_ARCHIVE_PIECE.get(), tag);
        spawnedGuardMask = tag.getIntOr("Guards", 0);
        spawnedArchivist = tag.getBooleanOr("Archivist", false);
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context,
            CompoundTag tag) {
        tag.putInt("Guards", spawnedGuardMask);
        tag.putBoolean("Archivist", spawnedArchivist);
    }

    @Override
    public BlockPos getLocatorPosition() {
        return getWorldPos(WIDTH / 2, 1, 5);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBounds,
            ChunkPos chunkPos,
            BlockPos referencePos) {
        buildShell(level, chunkBounds);
        buildArchitecture(level, chunkBounds);
        buildTrials(level, chunkBounds);
        buildBossArena(level, chunkBounds);
        placeLoot(level, chunkBounds, random);
        spawnGuards(level, chunkBounds);
        spawnArchivist(level, chunkBounds);
    }

    private void buildShell(WorldGenLevel level, BoundingBox chunkBounds) {
        BlockState shell = Blocks.DEEPSLATE_TILES.defaultBlockState();
        generateBox(level, chunkBounds, 0, 0, 0, 16, 6, 108, shell, shell, false);
        generateAirBox(level, chunkBounds, 1, 1, 1, 15, 5, 107);

        BlockState path = Blocks.POLISHED_TUFF.defaultBlockState();
        generateBox(level, chunkBounds, 6, 0, 1, 10, 0, 107, path, path, false);
        generateBox(
                level,
                chunkBounds,
                8,
                0,
                1,
                8,
                0,
                107,
                Blocks.CUT_COPPER.weathering().exposed().defaultBlockState(),
                Blocks.CUT_COPPER.weathering().exposed().defaultBlockState(),
                false);

        generateAirBox(level, chunkBounds, 7, 1, 0, 9, 3, 1);
        generateAirBox(level, chunkBounds, 7, 1, 107, 9, 3, 108);

        for (int x : new int[] {0, 4, 8, 12, 16}) {
            for (int z : new int[] {0, 108}) {
                generateBox(
                        level,
                        chunkBounds,
                        x,
                        1,
                        z,
                        x,
                        8,
                        z,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        false);
            }
        }
        generateBox(
                level,
                chunkBounds,
                4,
                7,
                0,
                12,
                7,
                0,
                Blocks.CUT_COPPER.weathering().oxidized().defaultBlockState(),
                Blocks.CUT_COPPER.weathering().oxidized().defaultBlockState(),
                false);

        for (int x = 0; x < WIDTH; x += 2) {
            fillColumnDown(
                    level,
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    x,
                    -1,
                    0,
                    chunkBounds);
            fillColumnDown(
                    level,
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    x,
                    -1,
                    108,
                    chunkBounds);
        }
        for (int z = 0; z < DEPTH; z += 2) {
            fillColumnDown(
                    level,
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    0,
                    -1,
                    z,
                    chunkBounds);
            fillColumnDown(
                    level,
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    16,
                    -1,
                    z,
                    chunkBounds);
        }
    }

    private void buildArchitecture(WorldGenLevel level, BoundingBox chunkBounds) {
        BlockState pillar = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        for (int z = 4; z <= 104; z += 8) {
            generateBox(level, chunkBounds, 1, 1, z, 1, 5, z, pillar, pillar, false);
            generateBox(level, chunkBounds, 15, 1, z, 15, 5, z, pillar, pillar, false);
            generateBox(level, chunkBounds, 2, 5, z, 14, 5, z, pillar, pillar, false);
            placeBlock(level, Blocks.SEA_LANTERN.defaultBlockState(), 2, 3, z, chunkBounds);
            placeBlock(level, Blocks.SEA_LANTERN.defaultBlockState(), 14, 3, z, chunkBounds);
        }

        for (int z = 4; z <= 104; z += 10) {
            placeBlock(level, Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 1, 2, z, chunkBounds);
            placeBlock(level, Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 15, 2, z, chunkBounds);
            placeBlock(level, Blocks.AMETHYST_BLOCK.defaultBlockState(), 1, 3, z, chunkBounds);
            placeBlock(level, Blocks.AMETHYST_BLOCK.defaultBlockState(), 15, 3, z, chunkBounds);
        }

        for (int z : new int[] {12, 36, 56, 84, 100}) {
            for (int x = 3; x <= 13; x++) {
                int distance = Math.abs(x - 8);
                if (distance == 5 || distance == 3 || distance == 0) {
                    placeBlock(
                            level,
                            distance == 0
                                    ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                                    : Blocks.CUT_COPPER.weathering().oxidized().defaultBlockState(),
                            x,
                            0,
                            z,
                            chunkBounds);
                }
            }
        }

        BlockState eastTorch = Blocks.WALL_TORCH.defaultBlockState()
                .setValue(WallTorchBlock.FACING, Direction.EAST);
        BlockState westTorch = Blocks.WALL_TORCH.defaultBlockState()
                .setValue(WallTorchBlock.FACING, Direction.WEST);
        for (int z : new int[] {8, 24, 40, 48, 60, 72, 88, 102}) {
            placeBlock(level, eastTorch, 1, 3, z, chunkBounds);
            placeBlock(level, westTorch, 15, 3, z, chunkBounds);
        }

        buildClockFloor(level, chunkBounds);
    }

    private void buildClockFloor(WorldGenLevel level, BoundingBox chunkBounds) {
        int centerX = 8;
        int centerZ = 92;
        for (int x = 3; x <= 13; x++) {
            for (int z = 87; z <= 97; z++) {
                int dx = x - centerX;
                int dz = z - centerZ;
                int distanceSquared = dx * dx + dz * dz;
                if (distanceSquared >= 20 && distanceSquared <= 30) {
                    placeBlock(
                            level,
                            Blocks.COPPER_BLOCK.weathering().oxidized().defaultBlockState(),
                            x,
                            0,
                            z,
                            chunkBounds);
                }
            }
        }
        for (int z = 89; z <= 92; z++) {
            placeBlock(level, Blocks.AMETHYST_BLOCK.defaultBlockState(), centerX, 0, z, chunkBounds);
        }
        for (int x = centerX; x <= 11; x++) {
            placeBlock(level, Blocks.AMETHYST_BLOCK.defaultBlockState(), x, 0, centerZ, chunkBounds);
        }
    }

    private void buildTrials(WorldGenLevel level, BoundingBox chunkBounds) {
        buildBarrier(level, chunkBounds, FIRST_DOOR_Z);
        placeBlock(
                level,
                ModBlocks.RESONANCE_TARGET.get().defaultBlockState(),
                8,
                3,
                FIRST_DOOR_Z - 1,
                chunkBounds);

        buildBarrier(level, chunkBounds, SECOND_DOOR_Z);
        placeBlock(
                level,
                ModBlocks.ECHO_PLATE.get().defaultBlockState(),
                6,
                1,
                SECOND_DOOR_Z - 3,
                chunkBounds);
        placeBlock(
                level,
                ModBlocks.ECHO_PLATE.get().defaultBlockState(),
                10,
                1,
                SECOND_DOOR_Z - 3,
                chunkBounds);

        buildBarrier(level, chunkBounds, THIRD_DOOR_Z);
        placeBlock(
                level,
                ModBlocks.RESONANCE_TARGET.get().defaultBlockState(),
                8,
                3,
                THIRD_DOOR_Z - 1,
                chunkBounds);

        for (int z : new int[] {18, 42, 64}) {
            generateBox(
                    level,
                    chunkBounds,
                    3,
                    1,
                    z,
                    3,
                    3,
                    z,
                    Blocks.TINTED_GLASS.defaultBlockState(),
                    Blocks.TINTED_GLASS.defaultBlockState(),
                    false);
            generateBox(
                    level,
                    chunkBounds,
                    13,
                    1,
                    z,
                    13,
                    3,
                    z,
                    Blocks.TINTED_GLASS.defaultBlockState(),
                    Blocks.TINTED_GLASS.defaultBlockState(),
                    false);
        }
    }

    private void buildBarrier(WorldGenLevel level, BoundingBox chunkBounds, int z) {
        BlockState barrier = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
        generateBox(level, chunkBounds, 1, 1, z, 15, 5, z, barrier, barrier, false);
        generateAirBox(level, chunkBounds, 8, 1, z, 8, 2, z);
        placeBlock(
                level,
                ModBlocks.ARCHIVE_DOOR.get().defaultBlockState()
                        .setValue(ArchiveDoorBlock.FACING, Direction.SOUTH),
                8,
                1,
                z,
                chunkBounds);
        placeBlock(level, Blocks.AMETHYST_BLOCK.defaultBlockState(), 8, 3, z, chunkBounds);
    }

    private void buildBossArena(WorldGenLevel level, BoundingBox chunkBounds) {
        buildBarrier(level, chunkBounds, BOSS_REWARD_GATE_Z);
        buildBarrier(level, chunkBounds, BOSS_EXIT_GATE_Z);
        placeBlock(
                level,
                ModBlocks.RESONANCE_TARGET.get().defaultBlockState(),
                3,
                3,
                BOSS_HOME_Z - 4,
                chunkBounds);
        placeBlock(
                level,
                ModBlocks.RESONANCE_TARGET.get().defaultBlockState(),
                13,
                3,
                BOSS_HOME_Z + 4,
                chunkBounds);
    }

    private void placeLoot(
            WorldGenLevel level,
            BoundingBox chunkBounds,
            RandomSource random) {
        createChest(level, chunkBounds, random, 6, 1, 7, ModLootTables.ARCHIVE_ENTRANCE);
        createChest(level, chunkBounds, random, 10, 1, 7, ModLootTables.ARCHIVE_ENTRANCE);
        createChest(level, chunkBounds, random, 6, 1, 33, ModLootTables.ARCHIVE_SIGIL);
        createChest(level, chunkBounds, random, 10, 1, 33, ModLootTables.ARCHIVE_SIGIL);
        createChest(level, chunkBounds, random, 6, 1, 103, ModLootTables.ARCHIVE_REWARD);
        createChest(level, chunkBounds, random, 10, 1, 103, ModLootTables.ARCHIVE_REWARD);
    }

    private synchronized void spawnGuards(WorldGenLevel level, BoundingBox chunkBounds) {
        for (int index = 0; index < GUARD_X.length; index++) {
            int mask = 1 << index;
            if ((spawnedGuardMask & mask) != 0) {
                continue;
            }
            BlockPos pos = getWorldPos(GUARD_X[index], 1, GUARD_Z[index]);
            if (!chunkBounds.isInside(pos)) {
                continue;
            }

            Zombie guard = EntityTypes.ZOMBIE.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (guard == null) {
                continue;
            }
            guard.setPersistenceRequired();
            guard.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
            guard.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(pos),
                    EntitySpawnReason.STRUCTURE,
                    null);
            level.addFreshEntityWithPassengers(guard);
            spawnedGuardMask |= mask;
        }
    }

    private synchronized void spawnArchivist(WorldGenLevel level, BoundingBox chunkBounds) {
        if (spawnedArchivist) {
            return;
        }
        BlockPos home = getWorldPos(8, 1, BOSS_HOME_Z);
        if (!chunkBounds.isInside(home)) {
            return;
        }
        ArchivistEntity archivist = ModEntities.ARCHIVIST.get()
                .create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (archivist == null) {
            return;
        }
        BlockPos rewardGate = getWorldPos(8, 1, BOSS_REWARD_GATE_Z);
        BlockPos exitGate = getWorldPos(8, 1, BOSS_EXIT_GATE_Z);
        archivist.snapTo(
                home.getX() + 0.5D,
                home.getY(),
                home.getZ() + 0.5D,
                180.0F,
                0.0F);
        archivist.initializeArchive(home, rewardGate, exitGate);
        level.addFreshEntityWithPassengers(archivist);
        spawnedArchivist = true;
    }
}
