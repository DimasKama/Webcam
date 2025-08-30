package ru.dimaskama.webcam.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.WebcamEvents;
import ru.dimaskama.webcam.command.WebcamconfigModCommand;

@EventBusSubscriber(modid = Webcam.MOD_ID)
public class WebcamNeoForgeEvents {

    @SubscribeEvent
    private static void onServerStartedEvent(ServerStartedEvent event) {
        WebcamNeoForge.setServer(event.getServer());
        WebcamEvents.onMinecraftServerStarted();
    }

    @SubscribeEvent
    private static void onServerTickEvent(ServerTickEvent.Post event) {
        WebcamEvents.onMinecraftServerTick();
    }

    @SubscribeEvent
    private static void onServerStoppingEvent(ServerStoppingEvent event) {
        WebcamEvents.onMinecraftServerStopping();
        WebcamNeoForge.setServer(null);
    }

    @SubscribeEvent
    private static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WebcamEvents.onPlayerDisconnected(player.getUUID());
        }
    }

    @SubscribeEvent
    private static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        WebcamconfigModCommand.register(event.getDispatcher());
    }

}
