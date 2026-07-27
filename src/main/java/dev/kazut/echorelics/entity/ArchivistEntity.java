package dev.kazut.echorelics.entity;

import dev.kazut.echorelics.block.ArchiveDoorBlock;
import dev.kazut.echorelics.block.ResonanceTargetBlock;
import dev.kazut.echorelics.echo.EchoActorRef;
import dev.kazut.echorelics.echo.EchoEffects;
import dev.kazut.echorelics.echo.EchoProvenance;
import dev.kazut.echorelics.echo.EchoSystem;
import dev.kazut.echorelics.echo.action.SlashEchoAction;
import dev.kazut.echorelics.echo.shape.OrientedSlashShape;
import dev.kazut.echorelics.registry.ModBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ArchivistEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_SHIELDED =
            SynchedEntityData.defineId(ArchivistEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int ECHO_DELAY_TICKS = 60;
    private static final int ECHO_WARNING_TICKS = 10;
    private static final float ECHO_DAMAGE = 5.0F;
    private static final int HOME_RADIUS = 9;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            getUUID(),
            Component.translatable("entity.echorelics.archivist"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);

    private @Nullable BlockPos archiveHome;
    private @Nullable BlockPos rewardGate;
    private @Nullable BlockPos exitGate;
    private boolean shieldTriggered;
    private boolean shieldBroken;

    public ArchivistEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_SHIELDED, false);
    }

    public void initializeArchive(BlockPos home, BlockPos gate) {
        initializeArchive(home, gate, gate);
    }

    public void initializeArchive(BlockPos home, BlockPos rewardGate, BlockPos exitGate) {
        archiveHome = home.immutable();
        this.rewardGate = rewardGate.immutable();
        this.exitGate = exitGate.immutable();
        setHomeTo(home, HOME_RADIUS);
        setPersistenceRequired();
    }

    public boolean isShieldActive() {
        return entityData.get(DATA_SHIELDED);
    }

    public boolean isShieldBroken() {
        return shieldBroken;
    }

    public BlockPos firstSealPosition() {
        return homePosition().offset(-5, 2, 4);
    }

    public BlockPos secondSealPosition() {
        return homePosition().offset(5, 2, -4);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        Vec3 origin = position();
        Vec3 direction = target.position().subtract(origin);
        direction = new Vec3(direction.x, 0.0D, direction.z);
        if (direction.lengthSqr() < 1.0E-6D) {
            Vec3 look = getLookAngle();
            direction = new Vec3(look.x, 0.0D, look.z);
        }
        direction = direction.normalize();
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            OrientedSlashShape shape = new OrientedSlashShape(
                    origin,
                    direction,
                    4.0D,
                    3.0D,
                    -0.25D,
                    2.5D);
            if (EchoSystem.schedule(
                    level,
                    EchoActorRef.livingEntity(this),
                    EchoProvenance.HOSTILE_RECORDED,
                    new SlashEchoAction(shape, ECHO_DAMAGE, true),
                    1,
                    ECHO_DELAY_TICKS,
                    ECHO_WARNING_TICKS)) {
                EchoEffects.hostileCapture(level, shape);
            }
        }
        return hurt;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isShieldActive() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.TRIAL_OMEN,
                    getX(),
                    getY() + 1.2D,
                    getZ(),
                    8,
                    0.45D,
                    0.7D,
                    0.45D,
                    0.02D);
            return false;
        }
        float phaseHealth = getMaxHealth() * 0.5F;
        boolean phaseGateApplies = !shieldTriggered
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (phaseGateApplies && getHealth() <= phaseHealth) {
            enterShield(level);
            return false;
        }

        boolean crossesPhaseGate = phaseGateApplies
                && damage >= getHealth() - phaseHealth;
        float appliedDamage = crossesPhaseGate
                ? Math.min(damage, getHealth() - phaseHealth)
                : damage;
        boolean hurt = super.hurtServer(level, source, appliedDamage);
        if (hurt && crossesPhaseGate && isAlive()) {
            setHealth(phaseHealth);
        }
        if (hurt && !shieldTriggered && getHealth() <= getMaxHealth() * 0.5F) {
            enterShield(level);
        }
        return hurt;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        evaluateShield(level);
        if (archiveHome != null && !isWithinHome()) {
            getNavigation().moveTo(
                    archiveHome.getX() + 0.5D,
                    archiveHome.getY(),
                    archiveHome.getZ() + 0.5D,
                    1.0D);
        }
        if (tickCount % 20 == 0) {
            level.sendParticles(
                    isShieldActive()
                            ? net.minecraft.core.particles.ParticleTypes.TRIAL_OMEN
                            : net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                    getX(),
                    getY() + 1.4D,
                    getZ(),
                    isShieldActive() ? 12 : 4,
                    0.55D,
                    0.85D,
                    0.55D,
                    0.01D);
        }
    }

    public void evaluateShield(ServerLevel level) {
        if (!shieldTriggered || shieldBroken) {
            return;
        }
        ensureSeal(level, firstSealPosition());
        ensureSeal(level, secondSealPosition());
        if (isPoweredSeal(level, firstSealPosition())
                && isPoweredSeal(level, secondSealPosition())) {
            breakShield(level);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (dead && level() instanceof ServerLevel level) {
            unlockExit(level);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ShieldTriggered", shieldTriggered);
        output.putBoolean("ShieldBroken", shieldBroken);
        writePos(output, "ArchiveHome", archiveHome);
        writePos(output, "RewardGate", rewardGate);
        writePos(output, "ExitGate", exitGate);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        shieldTriggered = input.getBooleanOr("ShieldTriggered", false);
        shieldBroken = input.getBooleanOr("ShieldBroken", false);
        boolean shieldActive = shieldTriggered && !shieldBroken;
        entityData.set(DATA_SHIELDED, shieldActive);
        bossEvent.setColor(shieldActive
                ? BossEvent.BossBarColor.RED
                : BossEvent.BossBarColor.PURPLE);
        archiveHome = readPos(input, "ArchiveHome").orElse(null);
        rewardGate = readPos(input, "RewardGate").orElse(null);
        exitGate = readPos(input, "ExitGate").orElse(null);
        if (archiveHome != null) {
            setHomeTo(archiveHome, HOME_RADIUS);
        }
    }

    private void enterShield(ServerLevel level) {
        shieldTriggered = true;
        shieldBroken = false;
        entityData.set(DATA_SHIELDED, true);
        ensureSeal(level, firstSealPosition());
        ensureSeal(level, secondSealPosition());
        resetSeal(level, firstSealPosition());
        resetSeal(level, secondSealPosition());
        bossEvent.setColor(BossEvent.BossBarColor.RED);
        level.playSound(
                null,
                blockPosition(),
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.HOSTILE,
                1.4F,
                0.55F);
    }

    private void breakShield(ServerLevel level) {
        shieldBroken = true;
        entityData.set(DATA_SHIELDED, false);
        bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                getX(),
                getY() + 1.2D,
                getZ(),
                48,
                0.9D,
                1.1D,
                0.9D,
                0.08D);
        level.playSound(
                null,
                blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.HOSTILE,
                1.5F,
                0.65F);
    }

    private void ensureSeal(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos) && !level.getBlockState(pos).is(ModBlocks.RESONANCE_TARGET.get())) {
            level.setBlock(pos, ModBlocks.RESONANCE_TARGET.get().defaultBlockState(), 3);
        }
    }

    private static boolean isPoweredSeal(ServerLevel level, BlockPos pos) {
        BlockState state = level.isLoaded(pos) ? level.getBlockState(pos) : null;
        return state != null
                && state.is(ModBlocks.RESONANCE_TARGET.get())
                && state.getValue(ResonanceTargetBlock.POWERED);
    }

    private static void resetSeal(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos)
                && level.getBlockState(pos).getBlock() instanceof ResonanceTargetBlock target) {
            target.deactivate(level, pos);
        }
    }

    private void unlockExit(ServerLevel level) {
        unlockGate(level, rewardGate);
        if (!java.util.Objects.equals(rewardGate, exitGate)) {
            unlockGate(level, exitGate);
        }
    }

    private static void unlockGate(ServerLevel level, @Nullable BlockPos gate) {
        if (gate == null || !level.isLoaded(gate)) {
            return;
        }
        BlockState state = level.getBlockState(gate);
        if (state.is(ModBlocks.ARCHIVE_DOOR.get())) {
            level.setBlock(
                    gate,
                    state.setValue(ArchiveDoorBlock.BOSS_UNLOCKED, true)
                            .setValue(ArchiveDoorBlock.OPEN, true),
                    3);
            level.playSound(
                    null,
                    gate,
                    SoundEvents.IRON_DOOR_OPEN,
                    SoundSource.BLOCKS,
                    1.2F,
                    0.7F);
        }
    }

    private BlockPos homePosition() {
        return archiveHome != null ? archiveHome : blockPosition();
    }

    private static void writePos(ValueOutput output, String name, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }
        ValueOutput child = output.child(name);
        child.putInt("X", pos.getX());
        child.putInt("Y", pos.getY());
        child.putInt("Z", pos.getZ());
    }

    private static Optional<BlockPos> readPos(ValueInput input, String name) {
        return input.child(name).map(child -> new BlockPos(
                child.getIntOr("X", 0),
                child.getIntOr("Y", 0),
                child.getIntOr("Z", 0)));
    }
}
