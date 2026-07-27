package dev.kazut.echorelics.item.capture;

import dev.kazut.echorelics.config.EchoRelicsConfig;
import dev.kazut.echorelics.echo.EchoEffects;
import dev.kazut.echorelics.echo.EchoSystem;
import dev.kazut.echorelics.echo.action.SlashEchoAction;
import dev.kazut.echorelics.echo.shape.OrientedSlashShape;
import dev.kazut.echorelics.item.EchoBladeItem;
import dev.kazut.echorelics.registry.ModEnchantments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

public final class EchoBladeCapture {
    private EchoBladeCapture() {
    }

    public static boolean capture(ItemStack stack, ServerPlayer player, ServerLevel level) {
        Registry<Enchantment> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int reverberation = Math.clamp(
                enchantmentLevel(stack, enchantments, ModEnchantments.REVERBERATION),
                0,
                3);
        int accelerando = Math.clamp(
                enchantmentLevel(stack, enchantments, ModEnchantments.ACCELERANDO),
                0,
                3);

        int replayCount = 1 + reverberation;
        int intervalTicks = EchoRelicsConfig.intervalForAccelerando(accelerando);
        int warningLeadTicks = EchoRelicsConfig.WARNING_LEAD_TICKS.getAsInt();
        float damage = snapshotDamage(player);
        if (damage <= 0.0F) {
            return false;
        }

        double yaw = Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        double firstYOffset = EchoRelicsConfig.SLASH_MIN_Y_OFFSET.getAsDouble();
        double secondYOffset = EchoRelicsConfig.SLASH_MAX_Y_OFFSET.getAsDouble();
        double minYOffset = Math.min(firstYOffset, secondYOffset);
        double maxYOffset = Math.max(firstYOffset, secondYOffset);
        if (minYOffset == maxYOffset) {
            maxYOffset = minYOffset + 0.01D;
        }
        OrientedSlashShape shape = new OrientedSlashShape(
                player.position(),
                forward,
                EchoRelicsConfig.SLASH_REACH.getAsDouble(),
                EchoRelicsConfig.SLASH_WIDTH.getAsDouble(),
                minYOffset,
                maxYOffset);
        SlashEchoAction action = new SlashEchoAction(shape, damage);

        if (EchoSystem.schedulePlayer(level, player.getUUID(), action, replayCount, intervalTicks, warningLeadTicks)) {
            EchoEffects.capture(level, shape);
            return true;
        }
        return false;
    }

    public static boolean captureEmptySwing(ServerPlayer player) {
        if (!player.isAlive()
                || player.isRemoved()
                || player.isSpectator()
                || player.isUsingItem()
                || !(player.level() instanceof ServerLevel level)) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();
        float attackStrength = player.getAttackStrengthScale(0.5F);
        if (!EchoBladeItem.isEchoBlade(stack) || attackStrength < 0.9F) {
            return false;
        }

        boolean scheduled = capture(stack, player, level);
        player.resetAttackStrengthTicker();
        return scheduled;
    }

    public static float cooldownMultiplier(float attackStrength) {
        float clampedStrength = Math.clamp(attackStrength, 0.0F, 1.0F);
        return 0.2F + clampedStrength * clampedStrength * 0.8F;
    }

    private static float snapshotDamage(ServerPlayer player) {
        return (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                * cooldownMultiplier(player.getAttackStrengthScale(0.5F))
                * EchoRelicsConfig.DAMAGE_MULTIPLIER.getAsDouble());
    }

    private static int enchantmentLevel(
            ItemStack stack,
            Registry<Enchantment> enchantments,
            ResourceKey<Enchantment> key) {
        return stack.getEnchantmentLevel(enchantments.getOrThrow(key));
    }
}
