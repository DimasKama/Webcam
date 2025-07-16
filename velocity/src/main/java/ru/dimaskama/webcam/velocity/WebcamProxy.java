package ru.dimaskama.webcam.velocity;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import ru.dimaskama.webcam.velocity.config.ProxyConfig;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WebcamProxy extends Thread {

    private final BlockingQueue<RawPacket> packetQueue = new LinkedBlockingQueue<>();
    private final WebcamVelocity plugin;
    private final DatagramSocket socket;
    private final PacketProcessingThread packetProcessingThread;
    private final Map<UUID, ProxyBridge> bridges = new ConcurrentHashMap<>();

    public WebcamProxy(WebcamVelocity plugin) throws Exception {
        this.plugin = plugin;
        setDaemon(true);
        setName("WebcamProxyThread");
        setUncaughtExceptionHandler((t, e) -> plugin.getLogger().error("Uncaught exception on WebcamProxyThread", e));
        int port = plugin.getConfig().getPort();
        String bindAddress = plugin.getConfig().getBindAddress();
        InetAddress address = null;
        try {
            if (!bindAddress.isEmpty()) {
                address = InetAddress.getByName(bindAddress);
            }
        } catch (Exception e) {
            plugin.getLogger().error("Failed to parse bind address \"{}\"", bindAddress, e);
            bindAddress = null;
        }
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port, address);
        } catch (BindException e) {
            if (address == null || bindAddress.equals("0.0.0.0")) {
                throw e;
            }
            plugin.getLogger().error("Failed to bind to address \"{}\", binding to wildcard IP instead", bindAddress);
            socket = new DatagramSocket(port);
        }
        this.socket = socket;
        packetProcessingThread = new PacketProcessingThread();
    }

    public int getBoundPort() {
        return socket.getLocalPort();
    }

    public SecretMessage onSecretMessage(RegisteredServer server, UUID playerUuid, SecretMessage secret) {
        InetSocketAddress serverMcAddress = server.getServerInfo().getAddress();
        InetAddress serverMcAddressInetAddress = serverMcAddress.getAddress();
        String ip = serverMcAddressInetAddress != null ? serverMcAddressInetAddress.getHostAddress() : serverMcAddress.getHostString();
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
                plugin.getLogger().warn("Failed to parse Webcam host from secret packet", e);
            }
        }
        InetSocketAddress serverAddress;
        try {
            serverAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ProxyConfig config = plugin.getConfig();
        SecretMessage modified = new SecretMessage(secret.secret(), config.getPort(), secret.keepAlivePeriod(), config.getHost());
        ProxyBridge bridge = new ProxyBridge(playerUuid, serverAddress);
        bridges.put(playerUuid, bridge);
        return modified;
    }

    public boolean resetBridge(UUID playerUuid) {
        return bridges.remove(playerUuid) != null;
    }

    @Override
    public void run() {
        try {
            packetProcessingThread.start();
            byte[] packetBuf = new byte[4096];
            while (!isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(packetBuf, packetBuf.length);
                    socket.receive(packet);
                    long timestamp = System.currentTimeMillis();
                    int len = packet.getLength();
                    byte[] data = new byte[len];
                    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, len);
                    packetQueue.add(new RawPacket(data, packet.getSocketAddress(), timestamp));
                } catch (Exception e) {
                    if (!(e instanceof SocketException && e.getCause() instanceof AsynchronousCloseException)) {
                        plugin.getLogger().error("Socket read error", e);
                    }
                }
            }
        } finally {
            socket.close();
            try {
                packetProcessingThread.join(100L);
            } catch (InterruptedException ignored) {}
        }
    }

    public void send(SocketAddress address, byte[] packet) throws Exception {
        DatagramPacket datagram = new DatagramPacket(packet, packet.length, address);
        socket.send(datagram);
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        socket.close();
    }

    private record RawPacket(
            byte[] data,
            SocketAddress address,
            long timestamp
    ) {}

    private class PacketProcessingThread extends Thread {

        public PacketProcessingThread() {
            setDaemon(true);
            setName("WebcamProxyPacketProcessingThread");
            setUncaughtExceptionHandler((t, e) -> plugin.getLogger().error("Uncaught exception on WebcamProxyPacketProcessingThread", e));
        }

        @Override
        public void run() {
            while (!isClosed()) {
                try {
                    RawPacket rawPacket = packetQueue.poll(10L, TimeUnit.MILLISECONDS);
                    if (rawPacket != null) {
                        if (System.currentTimeMillis() - rawPacket.timestamp <= 15000L) {
                            try {
                                byte[] data = rawPacket.data;
                                byte magic = data[0];
                                if (magic == (byte) 0b11101110) {
                                    // S2C. Check the server address and forward the message
                                    UUID playerUuid = getPlayerUuid(rawPacket.data);
                                    ProxyBridge bridge = bridges.get(playerUuid);
                                    if (bridge != null && areAddressesSame(bridge.getServerAddress(), rawPacket.address)) {
                                        bridge.lockAddress();
                                        SocketAddress playerAddress = bridge.getPlayerAddress();
                                        if (playerAddress != null) {
                                            send(playerAddress, data);
                                        }
                                    }
                                } else if (magic == (byte) 0b11001100) {
                                    // C2S. Simply forward the message
                                    UUID playerUuid = getPlayerUuid(rawPacket.data);
                                    ProxyBridge bridge = bridges.get(playerUuid);
                                    if (bridge != null) {
                                        bridge.updatePlayerAddress(rawPacket.address);
                                        send(bridge.getServerAddress(), data);
                                    }
                                }
                            } catch (Exception ignored) {
                                // Ignore invalid packets
                            }
                        } else {
                            plugin.getLogger().debug("Dropping packets! Is server overloaded?");
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().error("Error processing packet", e);
                }
            }
        }

        private static UUID getPlayerUuid(byte[] packetData) {
            ByteBuffer buffer = ByteBuffer.wrap(packetData, 1, 16);
            return new UUID(buffer.getLong(), buffer.getLong());
        }

        private static boolean areAddressesSame(InetSocketAddress inetSocketAddress1, SocketAddress socketAddress2) {
            return socketAddress2 instanceof InetSocketAddress inetSocketAddress2
                    && inetSocketAddress1.getAddress().equals(inetSocketAddress2.getAddress())
                    && inetSocketAddress1.getPort() == inetSocketAddress2.getPort();
        }

    }

}
