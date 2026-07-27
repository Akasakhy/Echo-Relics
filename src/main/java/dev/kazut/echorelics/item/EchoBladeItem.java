package dev.kazut.echorelics.item;

import dev.kazut.echorelics.item.capture.EchoBladeCapture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EchoBladeItem extends Item {
    public EchoBladeItem(Properties properties) {
        super(properties);
    }

    public static boolean isEchoBlade(ItemStack stack) {
        return stack.getItem() instanceof EchoBladeItem;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        EchoBladeCapture.capture(stack, player, level);
    }
}
