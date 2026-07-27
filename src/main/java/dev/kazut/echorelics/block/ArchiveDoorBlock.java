package dev.kazut.echorelics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class ArchiveDoorBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ArchiveDoorBlock> CODEC = simpleCodec(ArchiveDoorBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty BOSS_UNLOCKED = BooleanProperty.create("boss_unlocked");

    private static final int PLATE_DISTANCE = 3;
    private static final int PLATE_SIDE_OFFSET = 2;
    private static final int OCCUPIED_RECHECK_TICKS = 10;
    private static final VoxelShape Z_CLOSED_SHAPE =
            Block.box(0.0D, 0.0D, 5.0D, 16.0D, 32.0D, 11.0D);
    private static final VoxelShape X_CLOSED_SHAPE =
            Block.box(5.0D, 0.0D, 0.0D, 11.0D, 32.0D, 16.0D);

    public ArchiveDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(BOSS_UNLOCKED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return state.getValue(OPEN)
                ? Shapes.empty()
                : closedShape(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return state.getValue(OPEN)
                ? Shapes.empty()
                : closedShape(state.getValue(FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            refresh(serverLevel, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refresh(level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, BOSS_UNLOCKED);
    }

    public void refresh(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) {
            return;
        }
        if (state.getValue(BOSS_UNLOCKED)) {
            if (!state.getValue(OPEN)) {
                level.setBlock(pos, state.setValue(OPEN, true), 3);
            }
            return;
        }

        Direction inputDirection = state.getValue(FACING);
        boolean open = state.getValue(OPEN);
        BlockState targetState = loadedState(level, pos.relative(inputDirection).above(2));
        boolean shouldOpen = targetState != null
                && targetState.getBlock() instanceof ResonanceTargetBlock
                && targetState.getValue(ResonanceTargetBlock.POWERED);

        if (targetState == null || !(targetState.getBlock() instanceof ResonanceTargetBlock)) {
            BlockPos plateCenter = pos.relative(inputDirection, PLATE_DISTANCE);
            BlockState leftPlate = loadedState(
                    level,
                    plateCenter.relative(inputDirection.getCounterClockWise(), PLATE_SIDE_OFFSET));
            BlockState rightPlate = loadedState(
                    level,
                    plateCenter.relative(inputDirection.getClockWise(), PLATE_SIDE_OFFSET));
            shouldOpen = open
                    ? isPoweredPlate(leftPlate) && isPoweredPlate(rightPlate)
                    : isActivelyHeldPlate(leftPlate) && isActivelyHeldPlate(rightPlate);
        }

        if (open && !shouldOpen && hasPlayerInPassage(level, pos)) {
            level.scheduleTick(pos, this, OCCUPIED_RECHECK_TICKS);
            return;
        }
        if (open != shouldOpen) {
            level.setBlock(pos, state.setValue(OPEN, shouldOpen), 3);
            level.playSound(
                    null,
                    pos,
                    shouldOpen ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                    SoundSource.BLOCKS,
                    0.9F,
                    shouldOpen ? 1.25F : 0.9F);
        }
    }

    private static boolean hasPlayerInPassage(ServerLevel level, BlockPos pos) {
        AABB passage = new AABB(
                pos.getX() - 0.05D,
                pos.getY(),
                pos.getZ() - 0.05D,
                pos.getX() + 1.05D,
                pos.getY() + 2.0D,
                pos.getZ() + 1.05D);
        return !level.getEntitiesOfClass(
                Player.class,
                passage,
                player -> player.isAlive() && !player.isSpectator()).isEmpty();
    }

    private static boolean isPoweredPlate(@Nullable BlockState state) {
        return state != null
                && state.getBlock() instanceof EchoPlateBlock
                && state.getValue(EchoPlateBlock.POWERED);
    }

    private static boolean isActivelyHeldPlate(@Nullable BlockState state) {
        return isPoweredPlate(state) && !state.getValue(EchoPlateBlock.RELEASING);
    }

    private static @Nullable BlockState loadedState(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) ? level.getBlockState(pos) : null;
    }

    private static VoxelShape closedShape(Direction facing) {
        return facing.getAxis() == Direction.Axis.X ? X_CLOSED_SHAPE : Z_CLOSED_SHAPE;
    }
}
