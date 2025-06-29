package ru.dimaskama.webcam.message;

import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.WebcamService;
import ru.dimaskama.webcam.server.WebcamServer;

import java.util.UUID;

public class ServerMessaging {

    public static void init() {
        WebcamService service = Webcam.getService();
        service.registerChannel(Channel.SECRET_REQUEST, ServerMessaging::onSecretRequest);
        service.registerChannel(Channel.SECRET, null);
    }

    private static void onSecretRequest(UUID playerUuid, SecretRequestMessage message) {
        WebcamServer server = WebcamServer.getInstance();
        if (server != null && !server.isClosed()) {
            if (Webcam.isClientVersionCompatible(message.version())) {
                Webcam.getLogger().info("Sending secret to " + playerUuid);
                Webcam.getService().sendToPlayer(playerUuid, new SecretMessage(
                        server.getOrCreatePlayerState(playerUuid).getSecret(),
                        server.getSocket().getPort(),
                        server.getKeepAlivePeriod(),
                        server.getHost()
                ));
            } else {
                Webcam.getLogger().info("Client protocol version is incompatible. Not replying on secret request");
            }
        }
    }

    @FunctionalInterface
    public interface ServerHandler<T extends Message> {

        void handle(UUID playerUuid, T message);

    }

}
