package dev.kazut.echorelics.echo.action;

import dev.kazut.echorelics.echo.EchoAction;
import dev.kazut.echorelics.echo.EchoBlockInteraction;
import dev.kazut.echorelics.echo.EchoDamageExecutor;
import dev.kazut.echorelics.echo.EchoEffects;
import dev.kazut.echorelics.echo.EchoExecutionContext;
import dev.kazut.echorelics.echo.EchoTargetPolicy;
import dev.kazut.echorelics.echo.shape.OrientedSlashShape;
import dev.kazut.echorelics.config.EchoRelicsConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public final class SlashEchoAction implements EchoAction {
    private final OrientedSlashShape shape;
    private final float damage;
    private final boolean hostileVisuals;

    public SlashEchoAction(OrientedSlashShape shape, float damage) {
        this(shape, damage, false);
    }

    public SlashEchoAction(OrientedSlashShape shape, float damage, boolean hostileVisuals) {
        this.shape = shape;
        this.damage = Math.max(0.0F, damage);
        this.hostileVisuals = hostileVisuals;
    }

    @Override
    public Vec3 origin() {
        return shape.origin();
    }

    @Override
    public void warn(ServerLevel level) {
        if (hostileVisuals) {
            EchoEffects.hostileWarning(level, shape);
        } else {
            EchoEffects.warning(level, shape);
        }
    }

    @Override
    public void execute(ServerLevel level, EchoExecutionContext context) {
        if (hostileVisuals) {
            EchoEffects.hostileReplay(level, shape);
        } else {
            EchoEffects.replay(level, shape);
        }
        EchoBlockInteraction.triggerSlash(level, shape, context);

        List<LivingEntity> targets = new ArrayList<>();
        level.getEntities(
                EntityTypeTest.<Entity, LivingEntity>forClass(LivingEntity.class),
                shape.broadPhaseBounds(),
                candidate -> EchoTargetPolicy.canTarget(context, candidate)
                        && shape.intersects(candidate.getBoundingBox()),
                targets,
                EchoRelicsConfig.MAX_TARGETS_PER_REPLAY.getAsInt());

        for (LivingEntity target : targets) {
            if (EchoDamageExecutor.hurt(level, context, target, shape.origin(), damage)) {
                EchoEffects.hit(level, target);
            }
        }
    }
}
