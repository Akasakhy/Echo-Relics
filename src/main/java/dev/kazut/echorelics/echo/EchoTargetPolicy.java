package dev.kazut.echorelics.echo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public final class EchoTargetPolicy {
    private EchoTargetPolicy() {
    }

    public static boolean canTarget(EchoExecutionContext context, LivingEntity target) {
        LivingEntity actor = context.actor();
        if (target == actor
                || !target.isAlive()
                || target.isRemoved()
                || target.isSpectator()
                || !target.isAttackable()) {
            return false;
        }
        if (target instanceof ArmorStand armorStand && armorStand.isMarker()) {
            return false;
        }
        return switch (context.provenance().alignment()) {
            case PLAYER -> canPlayerTarget(context.playerActor(), target);
            case HOSTILE -> target instanceof ServerPlayer
                    && (actor == null || (!actor.isAlliedTo(target) && !target.isAlliedTo(actor)));
            case NEUTRAL -> false;
        };
    }

    private static boolean canPlayerTarget(ServerPlayer owner, LivingEntity target) {
        if (owner == null) {
            return false;
        }
        if (target instanceof Player player) {
            return owner.canHarmPlayer(player);
        }
        if (target instanceof OwnableEntity ownable && ownable.getRootOwner() == owner) {
            return false;
        }
        return !owner.isAlliedTo(target) && !target.isAlliedTo(owner);
    }
}
