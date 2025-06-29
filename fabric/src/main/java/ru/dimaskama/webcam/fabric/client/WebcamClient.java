package ru.dimaskama.webcam.fabric.client;

import io.netty.channel.local.LocalAddress;
import net.minecraft.client.Minecraft;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.config.SyncedServerConfig;
import ru.dimaskama.webcam.message.SecretMessage;
import ru.dimaskama.webcam.net.*;
import ru.dimaskama.webcam.net.packet.*;

import javax.annotation.Nullable;
import java.net.*;
import java.nio.channels.AsynchronousCloseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WebcamClient extends Thread {

    private static WebcamClient instance;
    private final BlockingQueue<RawPacket> packetQueue = new LinkedBlockingQueue<>();
    private final ClientWebcamSocket socket;
    private final UUID playerUuid;
    private final UUID secret;
    private final SocketAddress socketAddress;
    private final int keepAlivePeriod;
    private final PacketProcessingThread packetProcessingThread = new PacketProcessingThread();
    private final Map<UUID, DisplayingVideo> displayingVideos = new ConcurrentHashMap<>();
    private volatile boolean authenticated;
    private volatile SyncedServerConfig serverConfig = new SyncedServerConfig();
    private long sequenceNumber = 0;
    private int frameNumber = 0;

    public WebcamClient(ClientWebcamSocket socket, UUID playerUuid, UUID secret, SocketAddress socketAddress, int keepAlivePeriod) {
        this.socket = socket;
        this.playerUuid = playerUuid;
        this.secret = secret;
        this.socketAddress = socketAddress;
        this.keepAlivePeriod = keepAlivePeriod;
        setDaemon(true);
        setName("WebcamClient");
        setUncaughtExceptionHandler((t, e) -> Webcam.getLogger().error("Uncaught exception on WebcamClient", e));
    }

    public static void initialize(UUID playerUuid, SocketAddress minecraftSocketAddress, SecretMessage secret) {
        shutdown();
        ClientWebcamSocket socket = null;
        try {
            socket = new ClientWebcamSocket();
        } catch (Exception e) {
            Webcam.getLogger().error("Socket opening error", e);
        }

        String ip;
        if (minecraftSocketAddress instanceof LocalAddress) {
            ip = "127.0.0.1";
        } else if (minecraftSocketAddress instanceof InetSocketAddress minecraftInetSocketAddress) {
            InetAddress inetAddress = minecraftInetSocketAddress.getAddress();
            ip = inetAddress != null ? inetAddress.getHostAddress() : minecraftInetSocketAddress.getHostString();
        } else {
            throw new IllegalArgumentException("minecraftSocketAddress is not InetSocketAddress");
        }
        int port = secret.serverPort();
        if (!secret.host().isEmpty()) {
            try {
                URI uri = new URI("webcam://" + secret.host());
                String host = uri.getHost();
                int hostPort = uri.getPort();
                if (host != null) {
                    ip = host;
                }
                if (hostPort > 0) {
                    port = hostPort;
                }
            } catch (Exception e) {
                Webcam.getLogger().warn("Failed to parse webcam host", e);
            }
        }
        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
        } catch (Exception e) {
            Webcam.getLogger().error("Invalid webcam address " + ip + ":" + port);
        }
        if (socket != null && socketAddress != null) {
            WebcamClient client = instance = new WebcamClient(socket, playerUuid, secret.secret(), socketAddress, secret.keepAlivePeriod());
            client.start();
            Webcam.getLogger().info("Connecting to webcam server on address " + ip + ":" + port);
        } else {
            Webcam.getLogger().error("Webcam client has not started due to errors!");
        }
    }

    public static void shutdown() {
        WebcamClient client = instance;
        instance = null;
        if (client != null) {
            try {
                client.close();
                Webcam.getLogger().info("Webcam client closed");
            } catch (Exception e) {
                Webcam.getLogger().error("Client shutdown error", e);
            }
        }
    }

    @Nullable
    public static WebcamClient getInstance() {
        return instance;
    }

    public ClientWebcamSocket getSocket() {
        return socket;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public SyncedServerConfig getServerConfig() {
        return serverConfig;
    }

    public Map<UUID, DisplayingVideo> getDisplayingVideos() {
        return displayingVideos;
    }

    public void sendFrame(byte[] jpgImage) {
        List<FrameChunk> chunks = FrameChunk.split(sequenceNumber, frameNumber++, jpgImage, serverConfig.mtu());
        sequenceNumber += chunks.size();
        for (FrameChunk chunk : chunks) {
            send(new FrameChunkC2SPacket(chunk));
        }
    }

    public void send(Packet packet) {
        try {
            socket.send(socketAddress, C2SPacket.encrypt(playerUuid, secret, packet));
        } catch (Exception e) {
            Webcam.getLogger().warn("Failed to send packet to server", e);
        }
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
        Minecraft.getInstance().execute(() -> {
            displayingVideos.values().removeIf(displayingVideo -> {
                displayingVideo.closeTexture();
                return true;
            });
        });
    }

    public void tick() {
        long time = System.currentTimeMillis();
        boolean debug = Webcam.isDebugMode();
        displayingVideos.values().removeIf(displayingVideo -> {
            if (time - displayingVideo.getLastChunkTime() > 5000L) {
                displayingVideo.closeTexture();
                if (debug) {
                    Webcam.getLogger().info("Removing displaying video " + displayingVideo.getUuid() + " as it was inactive for 5s");
                }
                return true;
            }
            return false;
        });
    }

    private class PacketProcessingThread extends Thread {

        public PacketProcessingThread() {
            setDaemon(true);
            setName("WebcamClientPacketProcessingThread");
            setUncaughtExceptionHandler((t, e) -> Webcam.getLogger().error("Uncaught exception on WebcamClientPacketProcessingThread", e));
        }

        @Override
        public void run() {
            long lastPacketTime = System.currentTimeMillis();
            long authPacketSentTime = 0L;
            while (!isClosed()) {
                try {
                    long time = System.currentTimeMillis();
                    if ((int) (time - lastPacketTime) >= 10 * keepAlivePeriod) {
                        Webcam.getLogger().warn("Server timed out");
                        close();
                        return;
                    }
                    if (!authenticated && time - authPacketSentTime > PacketType.AUTH.getTTL()) {
                        send(new AuthPacket(playerUuid, secret));
                        authPacketSentTime = time;
                    }
                    RawPacket rawPacket = packetQueue.poll(10L, TimeUnit.MILLISECONDS);
                    if (rawPacket != null) {
                        time = System.currentTimeMillis();
                        lastPacketTime = time;
                        Packet packet = null;
                        try {
                            packet = S2CPacket.decrypt(rawPacket.data(), secret);
                        } catch (Exception e) {
                            if (Webcam.isDebugMode()) {
                                Webcam.getLogger().warn("Ignoring invalid packet from server", e);
                            }
                        }
                        if (packet != null) {
                            if (time - rawPacket.timestamp() <= packet.getType().getTTL()) {
                                handlePacket( packet);
                            } else {
                                if (Webcam.isDebugMode()) {
                                    Webcam.getLogger().warn("Dropping packets! Is client overloaded?");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Webcam.getLogger().error("Error processing packet on client", e);
                }
            }
        }

        private void handlePacket(Packet packet) {
            if (packet instanceof ServerConfigPacket(SyncedServerConfig config)) {
                serverConfig = config;
                Minecraft.getInstance().execute(Webcams::updateImageDimension);
                return;
            }
            if (packet instanceof AuthAckPacket) {
                authenticated = true;
                Webcam.getLogger().info("Successfully authenticated to server");
                Minecraft.getInstance().execute(Webcams::updateCapturing);
                return;
            }
            if (packet instanceof KeepAlivePacket) {
                send(KeepAlivePacket.INSTANCE);
                return;
            }
            if (packet instanceof FrameChunkS2CPacket(VideoSource source, FrameChunk chunk)) {
                DisplayingVideo displayingVideo = displayingVideos.computeIfAbsent(source.getUuid(), DisplayingVideo::new);
                displayingVideo.onFrameChunk(source, chunk);
                return;
            }
            if (packet instanceof CloseSourceS2CPacket(UUID sourceUuid)) {
                Minecraft.getInstance().execute(() -> {
                    DisplayingVideo displayingVideo = displayingVideos.remove(sourceUuid);
                    displayingVideo.closeTexture();
                });
            }
        }

    }

}
