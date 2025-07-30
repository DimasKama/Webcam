package ru.dimaskama.webcam.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.WebcamService;
import ru.dimaskama.webcam.config.ServerConfig;
import ru.dimaskama.webcam.net.*;
import ru.dimaskama.webcam.net.packet.*;

import javax.annotation.Nullable;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class WebcamServer extends SimpleChannelInboundHandler<C2SPacket> {

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private static volatile EventLoopGroup eventLoopGroup;
    private static WebcamServer instance;
    private final Map<UUID, PlayerState> playerStateMap = new ConcurrentHashMap<>();
    private final int port;
    private final String address;
    private final String host;
    private final int keepAlivePeriod;
    private final Channel channel;
    private final KeepAliveThread keepAliveThread;
    private int minecraftTickCount;

    public WebcamServer(int port, String address, String host, int keepAlivePeriod) throws Exception {
        this.port = port;
        this.address = address.isEmpty() ? "0.0.0.0" : address;
        this.host = host;
        this.keepAlivePeriod = keepAlivePeriod;
        if (eventLoopGroup == null) {
            synchronized (WebcamServer.class) {
                if (eventLoopGroup == null) {
                    eventLoopGroup = new NioEventLoopGroup(r -> {
                        Thread thread = new Thread(r, "Webcam Server #" + THREAD_COUNT.getAndIncrement());
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
                        pipeline.addLast("decrypt_decode_c2s", new DecryptDecodeInboundC2SHandler(WebcamServer.this));
                        pipeline.addLast("packet_handler", WebcamServer.this);
                        pipeline.addLast("encrypt_s2c", new EncryptOutboundS2CHandler());
                        pipeline.addLast("encode_s2c", new EncodeOutboundS2CHandler());
                    }
                })
                .bind(new InetSocketAddress(address.isEmpty() ? null : InetAddress.getByName(address), port))
                .sync()
                .channel();
        keepAliveThread = new KeepAliveThread(this);
        keepAliveThread.start();
    }

    public static void initialize(ServerConfig config) {
        shutdown();
        String host = config.host();
        if (!host.isEmpty()) {
            try {
                new URI("webcam://" + host);
                Webcam.getLogger().info("Webcam host is \"" + host + "\"");
            } catch (URISyntaxException e) {
                Webcam.getLogger().warn("Failed to parse Webcam host", e);
                host = "";
            }
        }
        try {
            int port = config.port();
            String address = config.bindAddress();
            int keepAlivePeriod = config.keepAlivePeriod();
            WebcamServer server = instance = new WebcamServer(port, address, host, keepAlivePeriod);
            Webcam.getLogger().info("Webcam server started on " + server.address + ":" + server.port + (host.isEmpty() ? "" : ". Host: " + host));
        } catch (Exception e) {
            Webcam.getLogger().warn("Failed to open Webcam server", e);
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
                Webcam.getLogger().error("Webcam server close error", e);
            }
        }
    }

    @Nullable
    public static WebcamServer getInstance() {
        return instance;
    }

    public int getPort() {
        return port;
    }

    public int getKeepAlivePeriod() {
        return keepAlivePeriod;
    }

    public String getHost() {
        return host;
    }

    public PlayerState getOrCreatePlayerState(UUID playerUuid, String playerName) {
        return playerStateMap.computeIfAbsent(playerUuid, u -> new PlayerState(u, playerName));
    }

    @Nullable
    public PlayerState getPlayerState(UUID playerUuid) {
        return playerStateMap.get(playerUuid);
    }

    public void disconnectPlayer(UUID playerUuid) {
        playerStateMap.remove(playerUuid);
    }

    public void send(PlayerState target, Packet packet) {
        send(new S2CPacket(target, packet));
    }

    public void send(S2CPacket packet) {
        channel.writeAndFlush(packet);
    }

    public void sendBatching(PlayerState target, Packet packet) {
        sendBatching(new S2CPacket(target, packet));
    }

    public void sendBatching(S2CPacket packet) {
        channel.write(packet);
    }

    public void sendBatching(S2CEncodedPacket packet) {
        channel.write(packet);
    }

    public void flushChannel() {
        channel.flush();
    }

    public void broadcast(Packet packet) {
        broadcastNoRelease(Unpooled.buffer(packet.getEstimatedSizeWithId()), packet, p -> true);
    }

    private void broadcastNoRelease(ByteBuf buf, Packet packet, Predicate<PlayerState> filter) {
        packet.encodeWithId(buf);
        playerStateMap.values().forEach(p -> {
            if (p.isAuthenticated() && filter.test(p)) {
                sendBatching(new S2CEncodedPacket(p, buf.retainedDuplicate()));
            }
        });
        flushChannel();
    }

    private void broadcastVideoNearby(UUID player, ByteBuf buf, Packet packet, double maxDist, boolean includeSelf, boolean force) {
        try {
            Webcam.getService().acceptForNearbyPlayers(
                    player,
                    maxDist,
                    players -> broadcastNoRelease(buf, packet, p ->
                            (force || (p.hasViewPermission() && p.canShowWebcams() && !p.isSourceBlocked(player)))
                            && players.contains(p.getUuid())
                            && (includeSelf || !p.getUuid().equals(player)))
            );
        } finally {
            buf.release();
        }
    }

    public void sendKeepAlives() {
        long time = System.currentTimeMillis();
        long max = keepAlivePeriod * 10L;
        ByteBuf unpooledBuf = Unpooled.buffer(KeepAlivePacket.INSTANCE.getEstimatedSizeWithId());
        KeepAlivePacket.INSTANCE.encodeWithId(unpooledBuf);
        playerStateMap.values().forEach(player -> {
            if (player.isAuthenticated()) {
                if ((time - player.getLastKeepAlive()) > max) {
                    player.setAuthenticated(false);
                    Webcam.getLogger().info(player.getName() + " timed out");
                } else {
                    sendBatching(new S2CEncodedPacket(player, unpooledBuf.retainedDuplicate()));
                }
            }
        });
        flushChannel();
    }

    public void close() {
        try {
            channel.close().sync();
        } catch (Exception e) {
            Webcam.getLogger().error("Failed to close Webcam server channel", e);
        }
        keepAliveThread.interrupt();
    }

    public void minecraftTick() {
        if ((minecraftTickCount++ % Webcam.getServerConfig().getData().permissionCheckPeriod()) == 0L) {
            WebcamService service = Webcam.getService();
            playerStateMap.values().forEach(p -> updatePermissions(service, p));
        }
    }

    private void updatePermissions(WebcamService service, PlayerState player) {
        boolean broadcast = service.checkPermission(player.getUuid(), Webcam.WEBCAM_BROADCAST_PERMISSION, true);
        boolean view = service.checkPermission(player.getUuid(), Webcam.WEBCAM_VIEW_PERMISSION, true);
        boolean prevBroadcast = player.updateBroadcastPermission(broadcast);
        boolean prevView = player.updateViewPermission(view);
        if (broadcast != prevBroadcast || view != prevView) {
            send(player, new PermissionsS2CPacket(broadcast, view));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, C2SPacket msg) {
        PlayerState sender = msg.sender();
        Packet packet = msg.packet();
        if (packet instanceof AuthPacket(UUID playerUuid, UUID secret)) {
            if (playerUuid.equals(sender.getUuid()) && secret.equals(sender.getSecret())) {
                if (!sender.isAuthenticated()) {
                    updatePermissions(Webcam.getService(), sender);
                    sender.setAuthenticated(true);
                    Webcam.getLogger().info("Successfully authenticated " + sender.getName());
                } else {
                    Webcam.getLogger().warn(sender.getName() + " sent duplicate auth packet");
                }

                {
                    KnownSourcesS2CPacket addPlayerPacket = new KnownSourcesS2CPacket(List.of(new KnownSource(playerUuid, sender.getName())));
                    ByteBuf addPlayerPacketBuf = ctx.alloc().buffer(addPlayerPacket.getEstimatedSizeWithId());
                    try {
                        broadcastNoRelease(addPlayerPacketBuf, addPlayerPacket, p -> p != sender);
                    } finally {
                        addPlayerPacketBuf.release();
                    }
                }

                sendBatching(sender, new ServerConfigPacket(Webcam.getServerConfig().getData().synced()));
                sendBatching(sender, new PermissionsS2CPacket(sender.hasBroadcastPermission(), sender.hasViewPermission()));
                sendBatching(sender, AuthAckPacket.INSTANCE);

                flushChannel();

                int mtu = Webcam.getServerConfig().getData().synced().mtu();
                Iterator<KnownSource> playerSources = playerStateMap.values()
                        .stream()
                        .filter(PlayerState::isAuthenticated)
                        .map(p -> new KnownSource(p.getUuid(), p.getName()))
                        .iterator();
                for (KnownSourcesS2CPacket sourceListPacket : KnownSourcesS2CPacket.split(mtu, playerSources)) {
                    sendBatching(sender, sourceListPacket);
                }

                flushChannel();

            } else {
                Webcam.getLogger().warn(sender.getName() + " sent invalid auth packet");
            }
            return;
        }
        if (!sender.isAuthenticated()) {
            throw new IllegalStateException(sender.getName() + " sent a packet without authenticating");
        }
        switch (packet) {
            case KeepAlivePacket ignored ->
                    sender.setLastKeepAlive(System.currentTimeMillis());
            case VideoC2SPacket(NalUnit nal) -> {
                if (sender.hasBroadcastPermission()) {
                    UUID uuid = sender.getUuid();
                    ServerConfig config = Webcam.getServerConfig().getData();
                    double maxDistance = config.maxDisplayDistance();
                    VideoS2CPacket videoPacket = new VideoS2CPacket(
                            config.displayOnFace()
                                    ? new VideoSource.Face(uuid, maxDistance)
                                    : new VideoSource.AboveHead(uuid, maxDistance, config.displayShape(), config.displayOffsetY(), config.displaySize(), config.hideNicknames(), null),
                            nal
                    );
                    boolean includeSelf = config.displaySelfWebcam();
                    broadcastVideoNearby(uuid, ctx.alloc().buffer(videoPacket.getEstimatedSizeWithId()), videoPacket, maxDistance, includeSelf, false);
                } else {
                    send(sender, new PermissionsS2CPacket(false, sender.hasViewPermission()));
                }
            }
            case CloseSourceC2SPacket ignored -> {
                UUID uuid = sender.getUuid();
                boolean includeSelf = Webcam.getServerConfig().getData().displaySelfWebcam();
                CloseSourceS2CPacket reply = new CloseSourceS2CPacket(uuid);
                broadcastVideoNearby(uuid, ctx.alloc().buffer(reply.getEstimatedSizeWithId()), reply, Webcam.getServerConfig().getData().maxDisplayDistance(), includeSelf, true);
            }
            case ShowWebcamsC2SPacket(boolean showWebcams) ->
                    sender.setShowWebcams(showWebcams);
            case AddBlockedSourceC2SPacket(UUID uuid) ->
                    sender.addBlockedSource(uuid);
            case RemoveBlockedSourceC2SPacket(UUID uuid) ->
                    sender.removeBlockedSource(uuid);
            default -> throw new IllegalStateException("Can't handle packet " + packet + " from " + sender.getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Webcam.isDebugMode()) {
            Webcam.getLogger().warn("Failed to handle packet from client", cause);
        }
    }

}
