package ru.dimaskama.webcam.velocity;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import ru.dimaskama.webcam.velocity.config.ProxyConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WebcamProxy extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private static volatile EventLoopGroup eventLoopGroup;
    private final WebcamVelocity plugin;
    private final int port;
    private final String address;
    private final Channel channel;
    private final Map<UUID, ProxyBridge> bridges = new ConcurrentHashMap<>();

    public WebcamProxy(WebcamVelocity plugin) throws Exception {
        this.plugin = plugin;
        this.port = plugin.getConfig().getPort();
        String configuredAddress = plugin.getConfig().getBindAddress();
        this.address = configuredAddress.isEmpty() ? "0.0.0.0" : configuredAddress;
        if (eventLoopGroup == null) {
            synchronized (WebcamProxy.class) {
                if (eventLoopGroup == null) {
                    eventLoopGroup = new NioEventLoopGroup(r -> {
                        Thread thread = new Thread(r, "Webcam Proxy #" + THREAD_COUNT.getAndIncrement());
                        thread.setDaemon(true);
                        return thread;
                    });
                }
            }
        }
        channel = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("proxy", WebcamProxy.this);
                    }
                })
                .bind(new InetSocketAddress(address, port))
                .sync()
                .channel();
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
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

    public void close() {
        try {
            channel.close().sync();
        } catch (Exception e) {
            plugin.getLogger().warn("Failed to close Webcam proxy", e);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        InetSocketAddress sender = msg.sender();
        ByteBuf buf = msg.content();
        byte magic = buf.readByte();
        if (magic == (byte) 0b11101110) {
            // S2C. Check the server address and forward the message
            UUID playerUuid = new UUID(buf.readLong(), buf.readLong());
            ProxyBridge bridge = bridges.get(playerUuid);
            if (bridge != null && areAddressesSame(bridge.getServerAddress(), sender)) {
                bridge.lockAddress();
                InetSocketAddress playerAddress = bridge.getPlayerAddress();
                if (playerAddress != null) {
                    buf.resetReaderIndex();
                    channel.writeAndFlush(new DatagramPacket(buf.retain(), playerAddress));
                }
            }
        } else if (magic == (byte) 0b11001100) {
            // C2S. Simply forward the message
            UUID playerUuid = new UUID(buf.readLong(), buf.readLong());
            ProxyBridge bridge = bridges.get(playerUuid);
            if (bridge != null) {
                bridge.updatePlayerAddress(sender);
                buf.resetReaderIndex();
                channel.writeAndFlush(new DatagramPacket(buf.retain(), bridge.getServerAddress()));
            }
        }
    }

    private static boolean areAddressesSame(InetSocketAddress address1, InetSocketAddress address2) {
        return Objects.equals(address1.getAddress(), address2.getAddress()) && address1.getPort() == address2.getPort();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        plugin.getLogger().debug("Webcam proxy packet handler exception", cause);
    }

}
