package dev.kazut.echorelics.item;

import dev.kazut.echorelics.echo.EchoEffects;
import dev.kazut.echorelics.echo.EchoSystem;
import dev.kazut.echorelics.echo.action.SpawnAvatarAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class EchoSigilItem extends Item {
    public static final int SPAWN_DELAY_TICKS = 60;
    public static final int AVATAR_LIFETIME_TICKS = 100;
    public static final int COOLDOWN_TICKS = 160;
    private static final int WARNING_LEAD_TICKS = 10;

    public EchoSigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || !serverPlayer.isAlive()
                || serverPlayer.isSpectator()
                || serverPlayer.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        Vec3 origin = serverPlayer.position();
        SpawnAvatarAction action = new SpawnAvatarAction(
                origin,
                serverPlayer.getYRot(),
                AVATAR_LIFETIME_TICKS);
        if (!EchoSystem.schedulePlayer(
                serverLevel,
                serverPlayer.getUUID(),
                action,
                1,
                SPAWN_DELAY_TICKS,
                WARNING_LEAD_TICKS)) {
            return InteractionResult.FAIL;
        }

        serverPlayer.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        EchoEffects.sigilCapture(serverLevel, origin);
        return InteractionResult.SUCCESS_SERVER;
    }
}
