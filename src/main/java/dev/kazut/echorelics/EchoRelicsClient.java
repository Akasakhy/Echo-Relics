package dev.kazut.echorelics;

import dev.kazut.echorelics.client.ClientEchoEvents;
import dev.kazut.echorelics.client.ArchivistRenderer;
import dev.kazut.echorelics.client.EchoAvatarRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import dev.kazut.echorelics.registry.ModEntities;

@Mod(value = EchoRelics.MOD_ID, dist = Dist.CLIENT)
public final class EchoRelicsClient {
    public EchoRelicsClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.register(new ClientEchoEvents());
        modEventBus.addListener(this::registerRenderers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ECHO_AVATAR.get(), EchoAvatarRenderer::new);
        event.registerEntityRenderer(ModEntities.ARCHIVIST.get(), ArchivistRenderer::new);
    }
}
