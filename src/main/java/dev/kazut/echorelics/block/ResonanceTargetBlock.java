package dev.kazut.echorelics.block;

import com.mojang.serialization.MapCodec;
import dev.kazut.echorelics.echo.EchoAlignment;
import dev.kazut.echorelics.echo.EchoExecutionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class ResonanceTargetBlock extends Block implements EchoReactiveBlock {
    public static final MapCodec<ResonanceTargetBlock> CODEC = simpleCodec(ResonanceTargetBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int ACTIVE_TICKS = 80;

    public ResonanceTargetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public boolean onEchoHit(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            EchoExecutionContext context) {
        if (context.provenance().alignment() != EchoAlignment.PLAYER) {
            return false;
        }

        if (!state.getValue(POWERED)) {
            state = state.setValue(POWERED, true);
            level.setBlock(pos, state, 3);
            level.updateNeighborsAt(pos, this);
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    24,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.08D);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.AMETHYST_CLUSTER_HIT,
                    SoundSource.BLOCKS,
                    1.2F,
                    1.55F);
            ArchiveDeviceNetwork.notifyNearbyDoors(level, pos);
        }
        level.getBlockTicks().clearArea(new BoundingBox(pos));
        level.scheduleTick(pos, this, ACTIVE_TICKS);
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            ArchiveDeviceNetwork.notifyNearbyDoors(serverLevel, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            boolean movedByPiston) {
        ArchiveDeviceNetwork.notifyNearbyDoors(level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        deactivate(level, pos);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int ownSignal(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    public void deactivate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this) && state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            level.updateNeighborsAt(pos, this);
            ArchiveDeviceNetwork.notifyNearbyDoors(level, pos);
        }
    }
}
