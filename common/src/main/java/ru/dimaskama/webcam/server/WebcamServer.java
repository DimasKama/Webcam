package ru.dimaskama.webcam.server;

import ru.dimaskama.webcam.Utils;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.config.ServerConfig;
import ru.dimaskama.webcam.config.SyncedServerConfig;
import ru.dimaskama.webcam.net.*;
import ru.dimaskama.webcam.net.packet.*;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class WebcamServer extends Thread {

    private static WebcamServer instance;
    private final BlockingQueue<RawPacket> packetQueue = new LinkedBlockingQueue<>();
    private final Map<UUID, PlayerState> playerStateMap = new ConcurrentHashMap<>();
    private final ServerWebcamSocket socket;
    private final String host;
    private final int keepAlivePeriod;
    private final PacketProcessingThread packetProcessingThread = new PacketProcessingThread();

    public WebcamServer(ServerWebcamSocket socket, String host, int keepAlivePeriod) {
        this.socket = socket;
        this.host = host;
        this.keepAlivePeriod = keepAlivePeriod;
        setDaemon(true);
        setName("WebcamServer");
        setUncaughtExceptionHandler((t, e) -> Webcam.getLogger().error("Uncaught exception on WebcamServer thread", e));
    }

    public static void initialize(ServerConfig config) {
        shutdown();
        ServerWebcamSocket socket = null;
        try {
            checkHost(config.host());
            socket = new ServerWebcamSocket(config.port(), config.bindAddress());
        } catch (Exception e) {
            Webcam.getLogger().error("Socket opening error", e);
        }
        if (socket != null) {
            WebcamServer server = instance = new WebcamServer(socket, config.host(), config.keepAlivePeriod());
            server.start();
            Webcam.getLogger().info("Webcam server started on port " + config.port());
        } else {
            Webcam.getLogger().error("Webcam server has not started due to errors!");
        }
    }

    private static void checkHost(String host) throws Exception {
        if (!host.isEmpty()) {
            try {
                new URI("webcam://" + host);
                Webcam.getLogger().info("Webcam host is \"" + host + "\"");
            } catch (URISyntaxException e) {
                Webcam.getLogger().warn("Failed to parse Webcam host", e);
                throw e;
            }
        }
    }

    public static void shutdown() {
        WebcamServer server = instance;
        instance = null;
        if (server != null) {
            try {
                server.close();
                Webcam.getLogger().info("Webcam server closed");
            } catch (Exception e) {
                Webcam.getLogger().error("Server shutdown error", e);
            }
        }
    }

    @Nullable
    public static WebcamServer getInstance() {
        return instance;
    }

    public ServerWebcamSocket getSocket() {
        return socket;
    }

    public int getKeepAlivePeriod() {
        return keepAlivePeriod;
    }

    public String getHost() {
        return host;
    }

    public PlayerState getOrCreatePlayerState(UUID playerUuid) {
        return playerStateMap.computeIfAbsent(playerUuid, PlayerState::new);
    }

    @Nullable
    public PlayerState getPlayerState(UUID playerUuid) {
        return playerStateMap.get(playerUuid);
    }

    public void disconnectPlayer(UUID playerUuid) {
        playerStateMap.remove(playerUuid);
    }

    public void send(PlayerState player, Packet packet) {
        try {
            send(player, packet.encrypt(player.getSecret()));
        } catch (Exception e) {
            Webcam.getLogger().warn("Failed to encrypt packet " + packet + " to " + player.getUuid(), e);
        }
    }

    public void send(PlayerState player, byte[] encrypted) {
        try {
            socket.send(player.getSocketAddress(), S2CPacket.create(encrypted));
        } catch (Exception e) {
            Webcam.getLogger().warn("Failed to send packet to " + player.getSocketAddress(), e);
        }
    }

    public void broadcast(Packet packet, Predicate<PlayerState> playerStatePredicate) {
        byte[] tempBuf = Utils.TEMP_BUFFERS.get();
        ByteBuffer buffer = ByteBuffer.wrap(tempBuf);
        buffer.put(packet.getType().getId());
        packet.writeBytes(buffer);
        int len = buffer.position();
        playerStateMap.values().stream().filter(playerStatePredicate).forEach(p -> {
            if (p.isAuthenticated()) {
                try {
                    send(p, Encryption.encrypt(tempBuf, 0, len, p.getSecret()));
                } catch (Exception e) {
                    Webcam.getLogger().warn("Failed to encrypt packet " + packet + " to " + p.getUuid(), e);
                }
            }
        });
    }

    public void broadcastNearby(UUID player, Packet packet, double maxDist, boolean includeSelf) {
        Webcam.getService().acceptForNearbyPlayers(
                player,
                maxDist,
                players -> broadcast(packet, p -> players.contains(p.getUuid()) && (includeSelf || !p.getUuid().equals(player)))
        );
    }

    public void broadcastConfig(SyncedServerConfig config) {
        ServerConfigPacket packet = new ServerConfigPacket(config);
        playerStateMap.values().forEach(p -> send(p, packet));
    }

    @Override
    public void run() {
        packetProcessingThread.start();
        while (!socket.isClosed()) {
            try {
                packetQueue.add(socket.read());
            } catch (Exception e) {
                if (!(e instanceof SocketException && e.getCause() instanceof AsynchronousCloseException)) {
                    Webcam.getLogger().error("Socket read error", e);
                }
            }
        }
        try {
            packetProcessingThread.join(100L);
        } catch (InterruptedException ignored) {}
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        socket.close();
    }

    private class PacketProcessingThread extends Thread {

        public PacketProcessingThread() {
            setDaemon(true);
            setName("WebcamPacketProcessingThread");
            setUncaughtExceptionHandler((t, e) -> Webcam.getLogger().error("Uncaught exception on WebcamPacketProcessingThread", e));
        }

        @Override
        public void run() {
            long lastKeepAliveSent = 0L;
            while (!isClosed()) {
                long time = System.currentTimeMillis();
                try {
                    RawPacket rawPacket = packetQueue.poll(10L, TimeUnit.MILLISECONDS);
                    if (rawPacket != null) {
                        C2SPacket c2SPacket = null;
                        try {
                            c2SPacket = C2SPacket.decrypt(WebcamServer.this, rawPacket.data());
                            PlayerState sender = c2SPacket.sender();
                            SocketAddress lastSocketAddress = sender.getSocketAddress();
                            if (lastSocketAddress == null) {
                                sender.setSocketAddress(rawPacket.address());
                            } else if (!lastSocketAddress.equals(rawPacket.address())) {
                                throw new IllegalArgumentException("Same player but different address");
                            }
                        } catch (Exception e) {
                            if (Webcam.isDebugMode()) {
                                Webcam.getLogger().warn("Ignoring invalid packet from " + rawPacket.address(), e);
                            }
                        }
                        if (c2SPacket != null) {
                            if (time - rawPacket.timestamp() <= c2SPacket.packet().getType().getTTL()) {
                                handlePacket(rawPacket.timestamp(), c2SPacket);
                            } else {
                                if (Webcam.isDebugMode()) {
                                    Webcam.getLogger().warn("Dropping packets! Is server overloaded?");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Webcam.getLogger().error("Error processing packet on server", e);
                }
                if (time - lastKeepAliveSent >= keepAlivePeriod) {
                    playerStateMap.values().removeIf(playerState -> {
                        if (time - playerState.getLastKeepAlive() > 10L * keepAlivePeriod) {
                            Webcam.getLogger().info(playerState.getUuid() + " timed out");
                            return true;
                        }
                        if (playerState.isAuthenticated()) {
                            send(playerState, KeepAlivePacket.INSTANCE);
                        }
                        return false;
                    });
                    lastKeepAliveSent = time;
                }
            }
        }

        private void handlePacket(long timestamp, C2SPacket packet) {
            if (packet.packet() instanceof AuthPacket(UUID playerUuid, UUID secret)) {
                if (playerUuid.equals(packet.sender().getUuid()) && secret.equals(packet.sender().getSecret())) {
                    if (!packet.sender().isAuthenticated()) {
                        packet.sender().setAuthenticated(true);
                        Webcam.getLogger().info("Successfully authenticated " + playerUuid);
                    } else {
                        Webcam.getLogger().warn(packet.sender().getUuid() + " sent duplicate auth packet");
                    }
                    send(packet.sender(), new ServerConfigPacket(Webcam.SERVER_CONFIG.getData().synced()));
                    send(packet.sender(), AuthAckPacket.INSTANCE);
                } else {
                    Webcam.getLogger().warn(packet.sender().getUuid() + " sent invalid auth packet");
                }
                return;
            }
            if (!packet.sender().isAuthenticated()) {
                return;
            }
            if (packet.packet() instanceof KeepAlivePacket) {
                packet.sender().setLastKeepAlive(timestamp);
                return;
            }
            if (packet.packet() instanceof FrameChunkC2SPacket(FrameChunk chunk)) {
                // Filter unexpected packets
                if (packet.sender().onFrameChunk(chunk)) {
                    UUID uuid = packet.sender().getUuid();
                    ServerConfig config = Webcam.SERVER_CONFIG.getData();
                    double maxDistance = config.maxDisplayDistance();
                    FrameChunkS2CPacket chunkS2C = new FrameChunkS2CPacket(
                            config.displayOnFace()
                                    ? new VideoSource.Face(uuid, maxDistance)
                                    : new VideoSource.AboveHead(uuid, maxDistance, config.displayShape(), config.displayOffsetY(), config.displaySize(), config.hideNicknames(), null),
                            chunk
                    );
                    boolean includeSelf = config.displaySelfWebcam();
                    broadcastNearby(uuid, chunkS2C, maxDistance, includeSelf);
                }
                return;
            }
            if (packet.packet() instanceof CloseSourceC2SPacket) {
                UUID uuid = packet.sender().getUuid();
                boolean includeSelf = Webcam.SERVER_CONFIG.getData().displaySelfWebcam();
                broadcastNearby(uuid, new CloseSourceS2CPacket(uuid), Webcam.SERVER_CONFIG.getData().maxDisplayDistance(), includeSelf);
            }
        }

    }

}
