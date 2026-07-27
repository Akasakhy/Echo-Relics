package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            EchoRelics.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ECHO_RELICS = TABS.register(
            "echo_relics",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.echorelics.echo_relics"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.ECHO_BLADE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ECHO_BLADE);
                        output.accept(ModItems.AWAKENED_ECHO_BLADE);
                        output.accept(ModItems.ECHO_SIGIL);
                        output.accept(ModItems.ECHO_PLATE);
                        output.accept(ModItems.RESONANCE_TARGET);
                        output.accept(ModItems.ARCHIVE_DOOR);
                        addBooks(parameters.holders(), output, ModEnchantments.REVERBERATION);
                        addBooks(parameters.holders(), output, ModEnchantments.ACCELERANDO);
                    })
                    .build());

    private ModCreativeTabs() {
    }

    private static void addBooks(
            HolderLookup.Provider holders,
            CreativeModeTab.Output output,
            ResourceKey<Enchantment> key) {
        Holder.Reference<Enchantment> enchantment = holders.getOrThrow(key);
        for (int level = enchantment.value().getMinLevel(); level <= enchantment.value().getMaxLevel(); level++) {
            output.accept(EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level)));
        }
    }
}
