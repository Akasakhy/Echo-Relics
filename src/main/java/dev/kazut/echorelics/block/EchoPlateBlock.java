package dev.kazut.echorelics.block;

import com.mojang.serialization.MapCodec;
import dev.kazut.echorelics.entity.EchoAvatarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class EchoPlateBlock extends Block {
    public static final MapCodec<EchoPlateBlock> CODEC = simpleCodec(EchoPlateBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty RELEASING = BooleanProperty.create("releasing");
    public static final int RELEASE_GRACE_TICKS = 40;

    private static final VoxelShape UP_SHAPE = Block.column(14.0D, 0.0D, 1.0D);
    private static final VoxelShape DOWN_SHAPE = Block.column(14.0D, 0.0D, 0.5D);
    private static final AABB DETECTION_BOX = new AABB(
            0.0625D, 0.0D, 0.0625D,
            0.9375D, 0.75D, 0.9375D);
    private static final int RECHECK_TICKS = 10;

    public EchoPlateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(RELEASING, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return state.getValue(POWERED) ? DOWN_SHAPE : UP_SHAPE;
    }

    @Override
    protected void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (level instanceof ServerLevel serverLevel
                && isValidActivator(entity)) {
            refresh(serverLevel, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refresh(level, pos);
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
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int ownSignal(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP && state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, RELEASING);
    }

    public static void refreshAtAvatar(ServerLevel level, BlockPos avatarPos) {
        refreshIfPlate(level, avatarPos);
        refreshIfPlate(level, avatarPos.below());
    }

    public static void scheduleRefreshAfterAvatarRemoval(ServerLevel level, BlockPos avatarPos) {
        scheduleIfPlate(level, avatarPos);
        scheduleIfPlate(level, avatarPos.below());
    }

    public static boolean isValidActivator(Entity entity) {
        return entity instanceof EchoAvatarEntity
                || entity instanceof Player player && player.isAlive() && !player.isSpectator();
    }

    public void refresh(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) {
            return;
        }

        AABB bounds = DETECTION_BOX.move(pos);
        boolean activePlayer = !level.getEntitiesOfClass(
                Player.class,
                bounds,
                player -> player.isAlive() && !player.isSpectator()).isEmpty();
        boolean activeAvatar = !level.getEntitiesOfClass(
                EchoAvatarEntity.class,
                bounds,
                avatar -> !avatar.isRemoved()).isEmpty();
        boolean active = activePlayer || activeAvatar;
        boolean wasActive = state.getValue(POWERED);
        boolean wasReleasing = state.getValue(RELEASING);

        if (active) {
            level.getBlockTicks().clearArea(new BoundingBox(pos));
            if (!wasActive || wasReleasing) {
                level.setBlock(
                        pos,
                        state.setValue(POWERED, true).setValue(RELEASING, false),
                        3);
            }
            if (!wasActive) {
                level.updateNeighborsAt(pos, this);
                level.updateNeighborsAt(pos.below(), this);
                level.playSound(
                        null,
                        pos,
                        SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.25F);
                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.25D,
                        pos.getZ() + 0.5D,
                        12,
                        0.35D,
                        0.1D,
                        0.35D,
                        0.02D);
            }
            if (!wasActive || wasReleasing) {
                ArchiveDeviceNetwork.notifyNearbyDoors(level, pos);
            }
            level.scheduleTick(pos, this, RECHECK_TICKS);
            return;
        }

        if (wasActive && !wasReleasing) {
            level.setBlock(pos, state.setValue(RELEASING, true), 3);
            level.getBlockTicks().clearArea(new BoundingBox(pos));
            level.scheduleTick(pos, this, RELEASE_GRACE_TICKS);
            return;
        }

        if (wasActive) {
            level.setBlock(
                    pos,
                    state.setValue(POWERED, false).setValue(RELEASING, false),
                    3);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.STONE_BUTTON_CLICK_OFF,
                    SoundSource.BLOCKS,
                    0.8F,
                    0.9F);
            ArchiveDeviceNetwork.notifyNearbyDoors(level, pos);
        }
    }

    private static void refreshIfPlate(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos)
                && level.getBlockState(pos).getBlock() instanceof EchoPlateBlock plate) {
            plate.refresh(level, pos);
        }
    }

    private static void scheduleIfPlate(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos)
                && level.getBlockState(pos).getBlock() instanceof EchoPlateBlock plate) {
            level.getBlockTicks().clearArea(new BoundingBox(pos));
            level.scheduleTick(pos, plate, 1);
        }
    }
}
