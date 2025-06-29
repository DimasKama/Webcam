package ru.dimaskama.webcam;

import ru.dimaskama.webcam.server.WebcamServer;

import java.util.UUID;

public class WebcamEvents {

    public static void onMinecraftServerStarted() {
        Webcam.SERVER_CONFIG.loadOrCreate();
        WebcamServer.initialize(Webcam.SERVER_CONFIG.getData());
    }

    public static void onMinecraftServerStopping() {
        WebcamServer.shutdown();
    }

    public static void onPlayerDisconnected(UUID player) {
        WebcamServer server = WebcamServer.getInstance();
        if (server != null && !server.isClosed()) {
            server.disconnectPlayer(player);
        }
    }

}
