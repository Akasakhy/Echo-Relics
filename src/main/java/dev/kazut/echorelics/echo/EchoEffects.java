package dev.kazut.echorelics.echo;

import dev.kazut.echorelics.echo.shape.OrientedSlashShape;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EchoEffects {
    private EchoEffects() {
    }

    public static void capture(ServerLevel level, OrientedSlashShape shape) {
        Vec3 center = effectCenter(shape);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z,
                12, 0.45D, 0.7D, 0.45D, 0.02D);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.65F, 1.35F);
    }

    public static void warning(ServerLevel level, OrientedSlashShape shape) {
        Vec3 center = effectCenter(shape);
        level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z,
                18, 0.65D, 0.8D, 0.65D, 0.02D);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 1.75F);
    }

    public static void replay(ServerLevel level, OrientedSlashShape shape) {
        Vec3 center = effectCenter(shape);
        Vec3 right = new Vec3(-shape.forward().z, 0.0D, shape.forward().x);
        for (int i = -3; i <= 3; i++) {
            Vec3 point = center.add(right.scale(i * 0.32D));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.9F);
    }

    public static void hostileCapture(ServerLevel level, OrientedSlashShape shape) {
        Vec3 center = effectCenter(shape);
        level.sendParticles(
                ParticleTypes.TRIAL_OMEN,
                center.x,
                center.y,
                center.z,
                22,
                0.55D,
                0.8D,
                0.55D,
                0.02D);
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
                SoundSource.HOSTILE,
                0.9F,
                0.75F);
    }

    public static void hostileWarning(ServerLevel level, OrientedSlashShape shape) {
        Vec3 origin = shape.origin().add(0.0D, 1.0D, 0.0D);
        Vec3 forward = shape.forward();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        for (int step = 0; step <= 6; step++) {
            Vec3 point = origin.add(forward.scale(shape.reach() * step / 6.0D));
            level.sendParticles(
                    ParticleTypes.TRIAL_OMEN,
                    point.x,
                    point.y,
                    point.z,
                    1,
                    0.03D,
                    0.08D,
                    0.03D,
                    0.0D);
        }
        Vec3 center = origin.add(forward.scale(shape.reach() * 0.55D));
        for (int step = -2; step <= 2; step++) {
            Vec3 point = center.add(right.scale(shape.halfWidth() * step / 2.0D));
            level.sendParticles(
                    ParticleTypes.DUST_PLUME,
                    point.x,
                    point.y,
                    point.z,
                    1,
                    0.04D,
                    0.08D,
                    0.04D,
                    0.0D);
        }
        level.sendParticles(
                ParticleTypes.DUST_PLUME,
                center.x,
                center.y,
                center.z,
                8,
                0.18D,
                0.45D,
                0.18D,
                0.02D);
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM,
                SoundSource.HOSTILE,
                1.0F,
                0.65F);
    }

    public static void hostileReplay(ServerLevel level, OrientedSlashShape shape) {
        Vec3 center = effectCenter(shape);
        Vec3 right = new Vec3(-shape.forward().z, 0.0D, shape.forward().x);
        for (int i = -4; i <= 4; i++) {
            Vec3 point = center.add(right.scale(i * 0.3D));
            level.sendParticles(
                    ParticleTypes.TRIAL_OMEN,
                    point.x,
                    point.y,
                    point.z,
                    2,
                    0.08D,
                    0.08D,
                    0.08D,
                    0.0D);
        }
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE,
                1.2F,
                0.55F);
    }

    public static void hit(ServerLevel level, LivingEntity target) {
        Vec3 center = target.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z,
                8, 0.25D, 0.35D, 0.25D, 0.08D);
    }

    public static void sigilCapture(ServerLevel level, Vec3 origin) {
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                origin.x(),
                origin.y() + 0.1D,
                origin.z(),
                18,
                0.35D,
                0.1D,
                0.35D,
                0.02D);
        level.playSound(
                null,
                origin.x(),
                origin.y(),
                origin.z(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS,
                0.8F,
                0.7F);
    }

    public static void avatarWarning(ServerLevel level, Vec3 origin) {
        level.sendParticles(
                ParticleTypes.ENCHANT,
                origin.x(),
                origin.y() + 0.9D,
                origin.z(),
                20,
                0.35D,
                0.8D,
                0.35D,
                0.01D);
    }

    public static void avatarSpawn(ServerLevel level, Vec3 origin) {
        level.sendParticles(
                ParticleTypes.END_ROD,
                origin.x(),
                origin.y() + 0.9D,
                origin.z(),
                28,
                0.35D,
                0.8D,
                0.35D,
                0.04D);
        level.playSound(
                null,
                origin.x(),
                origin.y(),
                origin.z(),
                SoundEvents.AMETHYST_CLUSTER_PLACE,
                SoundSource.PLAYERS,
                1.0F,
                1.4F);
    }

    public static void avatarPresence(ServerLevel level, Vec3 origin, float yaw) {
        double radians = Math.toRadians(yaw);
        double shoulderX = Math.cos(radians) * 0.22D;
        double shoulderZ = Math.sin(radians) * 0.22D;
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                origin.x(),
                origin.y() + 1.55D,
                origin.z(),
                2,
                0.12D,
                0.12D,
                0.12D,
                0.0D);
        level.sendParticles(
                ParticleTypes.ENCHANT,
                origin.x() + shoulderX,
                origin.y() + 0.9D,
                origin.z() + shoulderZ,
                4,
                0.22D,
                0.5D,
                0.22D,
                0.0D);
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                origin.x(),
                origin.y() + 0.2D,
                origin.z(),
                2,
                0.2D,
                0.2D,
                0.2D,
                0.0D);
    }

    public static void avatarExpire(ServerLevel level, Vec3 origin) {
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                origin.x(),
                origin.y() + 0.9D,
                origin.z(),
                24,
                0.35D,
                0.8D,
                0.35D,
                0.05D);
        level.playSound(
                null,
                origin.x(),
                origin.y(),
                origin.z(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                0.7F,
                0.65F);
    }

    private static Vec3 effectCenter(OrientedSlashShape shape) {
        return shape.origin().add(shape.forward().scale(shape.reach() * 0.55D)).add(0.0D, 1.0D, 0.0D);
    }
}
