package dev.kazut.echorelics.entity;

import dev.kazut.echorelics.block.EchoPlateBlock;
import dev.kazut.echorelics.echo.EchoEffects;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EchoAvatarEntity extends Entity {
    private static final int DEFAULT_LIFETIME_TICKS = 100;

    private @Nullable UUID ownerId;
    private long expiryGameTime = Long.MAX_VALUE;

    public EchoAvatarEntity(EntityType<? extends EchoAvatarEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void initialize(UUID ownerId, int lifetimeTicks, float recordedYaw) {
        this.ownerId = ownerId;
        this.expiryGameTime = level().getGameTime() + Math.max(1, lifetimeTicks);
        setYRot(recordedYaw);
    }

    public @Nullable UUID ownerId() {
        return ownerId;
    }

    public long expiryGameTime() {
        return expiryGameTime;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);

        if (level() instanceof ServerLevel serverLevel) {
            EchoPlateBlock.refreshAtAvatar(serverLevel, blockPosition());
            if (tickCount % 5 == 0) {
                EchoEffects.avatarPresence(serverLevel, position(), getYRot());
            }
            if (ownerId == null || serverLevel.getGameTime() >= expiryGameTime) {
                expire();
            }
        }
    }

    public void expire() {
        if (isRemoved()) {
            return;
        }
        if (level() instanceof ServerLevel serverLevel && serverLevel.isLoaded(blockPosition())) {
            EchoEffects.avatarExpire(serverLevel, position());
        }
        discard();
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        // A fixed echo never participates in collision or movement resolution.
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        // Only Echo Plate is activated explicitly from tick(); vanilla plates stay unaffected.
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        super.onRemoval(reason);
        if (level() instanceof ServerLevel serverLevel) {
            EchoPlateBlock.scheduleRefreshAfterAvatarRemoval(serverLevel, blockPosition());
            if (ownerId != null) {
                EchoAvatarManager.onAvatarRemoved(serverLevel.getServer(), ownerId, getUUID());
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Ephemeral entities are never persisted.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Ephemeral entities are never persisted.
    }
}
