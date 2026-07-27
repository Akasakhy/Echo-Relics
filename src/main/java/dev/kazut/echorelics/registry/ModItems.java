package dev.kazut.echorelics.registry;

import dev.kazut.echorelics.EchoRelics;
import dev.kazut.echorelics.item.EchoBladeItem;
import dev.kazut.echorelics.item.EchoSigilItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoRelics.MOD_ID);

    public static final DeferredItem<EchoBladeItem> ECHO_BLADE = ITEMS.registerItem(
            "echo_blade",
            EchoBladeItem::new,
            properties -> properties.sword(ToolMaterial.IRON, 3.0F, -2.4F));

    public static final DeferredItem<EchoBladeItem> AWAKENED_ECHO_BLADE = ITEMS.registerItem(
            "awakened_echo_blade",
            EchoBladeItem::new,
            properties -> properties
                    .sword(ToolMaterial.DIAMOND, 4.0F, -2.4F)
                    .rarity(Rarity.EPIC));

    public static final DeferredItem<EchoSigilItem> ECHO_SIGIL = ITEMS.registerItem(
            "echo_sigil",
            EchoSigilItem::new,
            properties -> properties.stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> ECHO_PLATE =
            ITEMS.registerSimpleBlockItem(ModBlocks.ECHO_PLATE);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RESONANCE_TARGET =
            ITEMS.registerSimpleBlockItem(ModBlocks.RESONANCE_TARGET);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ARCHIVE_DOOR =
            ITEMS.registerSimpleBlockItem(ModBlocks.ARCHIVE_DOOR);

    private ModItems() {
    }
}
