package ru.dimaskama.webcam;

import ru.dimaskama.webcam.server.WebcamServer;

import java.util.UUID;

public class WebcamEvents {

    public static void onMinecraftServerStarted() {
        Webcam.getServerConfig().loadOrCreate();
        WebcamServer.initialize(Webcam.getServerConfig().getData());
    }

    public static void onMinecraftServerTick() {
        WebcamServer server = WebcamServer.getInstance();
        if (server != null) {
            server.minecraftTick();
        }
    }

    public static void onMinecraftServerStopping() {
        WebcamServer.shutdown();
    }

    public static void onPlayerDisconnected(UUID player) {
        WebcamServer server = WebcamServer.getInstance();
        if (server != null) {
            server.disconnectPlayer(player);
        }
    }

}
