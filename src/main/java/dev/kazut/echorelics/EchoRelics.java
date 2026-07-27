package dev.kazut.echorelics;

import dev.kazut.echorelics.config.EchoRelicsConfig;
import dev.kazut.echorelics.entity.ArchivistEntity;
import dev.kazut.echorelics.echo.EchoEvents;
import dev.kazut.echorelics.gametest.ModGameTests;
import dev.kazut.echorelics.network.ModNetworking;
import dev.kazut.echorelics.registry.ModCreativeTabs;
import dev.kazut.echorelics.registry.ModBlocks;
import dev.kazut.echorelics.registry.ModEntities;
import dev.kazut.echorelics.registry.ModItems;
import dev.kazut.echorelics.registry.ModStructures;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(EchoRelics.MOD_ID)
public final class EchoRelics {
    public static final String MOD_ID = "echorelics";

    public EchoRelics(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECE_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModGameTests.initialize(modEventBus);
        modEventBus.addListener(ModNetworking::registerPayloads);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::addToCreativeTab);
        NeoForge.EVENT_BUS.register(new EchoEvents());
        modContainer.registerConfig(ModConfig.Type.SERVER, EchoRelicsConfig.SPEC);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ARCHIVIST.get(), ArchivistEntity.createAttributes().build());
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.COMBAT.equals(event.getTabKey())) {
            event.accept(ModItems.ECHO_BLADE);
            event.accept(ModItems.AWAKENED_ECHO_BLADE);
            event.accept(ModItems.ECHO_SIGIL);
        }
    }
}
