package dev.kazut.echorelics.echo;

import dev.kazut.echorelics.registry.ModDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EchoDamageExecutor {
    private EchoDamageExecutor() {
    }

    public static boolean hurt(
            ServerLevel level,
            EchoExecutionContext context,
            LivingEntity target,
            Vec3 sourcePosition,
            float damage) {
        var damageType = level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(ModDamageTypes.ECHO_SLASH);
        DamageSource source = new DamageSource(damageType, null, context.actor(), sourcePosition);
        return target.hurtServer(level, source, damage);
    }
}
