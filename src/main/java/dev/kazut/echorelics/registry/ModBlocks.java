package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import dev.kazut.echorelics.block.ArchiveDoorBlock;
import dev.kazut.echorelics.block.EchoPlateBlock;
import dev.kazut.echorelics.block.ResonanceTargetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoRelics.MOD_ID);

    public static final DeferredBlock<EchoPlateBlock> ECHO_PLATE = BLOCKS.registerBlock(
            "echo_plate",
            EchoPlateBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F)
                    .sound(SoundType.AMETHYST));

    public static final DeferredBlock<ResonanceTargetBlock> RESONANCE_TARGET = BLOCKS.registerBlock(
            "resonance_target",
            ResonanceTargetBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_CYAN)
                    .strength(2.5F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> state.getValue(ResonanceTargetBlock.POWERED) ? 13 : 2));

    public static final DeferredBlock<ArchiveDoorBlock> ARCHIVE_DOOR = BLOCKS.registerBlock(
            "archive_door",
            ArchiveDoorBlock::new,
            properties -> properties.mapColor(MapColor.METAL)
                    .strength(4.0F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    private ModBlocks() {
    }
}
