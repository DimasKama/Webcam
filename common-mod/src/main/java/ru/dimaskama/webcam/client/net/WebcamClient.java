package ru.dimaskama.webcam.client.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.config.SyncedServerConfig;
import ru.dimaskama.webcam.client.*;
import ru.dimaskama.webcam.client.config.ClientConfig;
import ru.dimaskama.webcam.client.config.Resolution;
import ru.dimaskama.webcam.message.SecretMessage;
import ru.dimaskama.webcam.net.*;
import ru.dimaskama.webcam.net.packet.*;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WebcamClient extends SimpleChannelInboundHandler<Packet> implements WebcamOutputListener {

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private static volatile EventLoopGroup eventLoopGroup;
    private static WebcamClient instance;
    private final UUID playerUuid;
    private final UUID secret;
    private final SecretKey secretKey;
    private final InetSocketAddress serverAddress;
    private final int keepAlivePeriod;
    private final Channel channel;
    private final AdaptableH264Encoder h264Encoder;
    private final Map<UUID, DisplayingVideo> displayingVideos = new ConcurrentHashMap<>();
    private final Map<UUID, KnownSourceClient> knownSources = new ConcurrentHashMap<>();
    private volatile boolean wasAuthenticated;
    private volatile boolean authenticated;
    private volatile SyncedServerConfig serverConfig = new SyncedServerConfig();
    private volatile long lastPacketTime;
    private volatile boolean broadcastPermission = true;
    private volatile boolean viewPermission = true;
    private int nalSequenceNumber;
    private long lastAuthPacketSent;
    private int authAttempts;

    public WebcamClient(int port, String address, UUID playerUuid, UUID secret, int keepAlivePeriod) throws Exception {
        this.playerUuid = playerUuid;
        this.secret = secret;
        secretKey = Encryption.uuidToKey(secret);
        this.serverAddress = new InetSocketAddress(InetAddress.getByName(address), port);
        this.keepAlivePeriod = keepAlivePeriod;
        if (eventLoopGroup == null) {
            synchronized (WebcamClient.class) {
                if (eventLoopGroup == null) {
                    eventLoopGroup = new NioEventLoopGroup(r -> {
                        Thread thread = new Thread(r, "Webcam Client #" + THREAD_COUNT.getAndIncrement());
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
                        pipeline.addLast("decrypt_decode_s2c", new DecryptDecodeInboundS2CHandler(WebcamClient.this));
                        pipeline.addLast("packet_handler", WebcamClient.this);
                        pipeline.addLast("encode_encrypt_c2s", new EncodeEncryptOutboundC2SHandler(WebcamClient.this));
                    }
                })
                .bind(0)
                .sync()
                .channel();
        h264Encoder = new AdaptableH264Encoder();
        Webcams.addListener(this);
    }

    public static void initialize(UUID playerUuid, SocketAddress minecraftSocketAddress, SecretMessage secret) {
        shutdown();
        String address;
        if (minecraftSocketAddress instanceof LocalAddress) {
            address = "127.0.0.1";
        } else if (minecraftSocketAddress instanceof InetSocketAddress minecraftInetSocketAddress) {
            InetAddress inetAddress = minecraftInetSocketAddress.getAddress();
            address = inetAddress != null ? inetAddress.getHostAddress() : minecraftInetSocketAddress.getHostString();
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
                    address = host;
                }
                if (hostPort > 0) {
                    port = hostPort;
                }
            } catch (Exception e) {
                Webcam.getLogger().warn("Failed to parse Webcam server host. Using Minecraft server address", e);
            }
        }
        try {
            instance = new WebcamClient(port, address, playerUuid, secret.secret(), secret.keepAlivePeriod());
            Webcam.getLogger().info("Connecting to Webcam server on address " + address + ":" + port);
        } catch (Exception e) {
            Webcam.getLogger().error("Failed to start Webcam client", e);
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

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public UUID getSecret() {
        return secret;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public Map<UUID, DisplayingVideo> getDisplayingVideos() {
        return displayingVideos;
    }

    public Map<UUID, KnownSourceClient> getKnownSources() {
        return knownSources;
    }

    public boolean hasBroadcastPermission() {
        return broadcastPermission;
    }

    public boolean hasViewPermission() {
        return viewPermission;
    }

    public void send(Packet packet) {
        channel.writeAndFlush(packet);
    }

    public void sendBatching(Packet packet) {
        channel.write(packet);
    }

    public void flushChannel() {
        channel.flush();
    }

    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (authenticated) {
            wasAuthenticated = true;
            authAttempts = 0;
        } else {
            knownSources.values().removeIf(source -> {
                source.close();
                return true;
            });
        }
    }

    public void close() {
        Webcams.removeListener(this);
        h264Encoder.close();
        try {
            channel.close().sync();
        } catch (Exception e) {
            Webcam.getLogger().error("Failed to close Webcam client channel", e);
        }
        clearDisplayingVideos();
        setAuthenticated(false);
    }

    private void clearDisplayingVideos() {
        Minecraft.getInstance().execute(() ->
                displayingVideos.values().removeIf(displayingVideo -> {
                    displayingVideo.close();
                    return true;
                })
        );
    }

    public void minecraftTick() {
        long time = System.currentTimeMillis();
        displayingVideos.values().removeIf(displayingVideo -> {
            if (time - displayingVideo.getLastChunkTime() > 5000L) {
                displayingVideo.close();
                Webcam.getLogger().info("Removing displaying video " + displayingVideo.getUuid() + " as it was inactive for 5s");
                return true;
            }
            return false;
        });
        if (authenticated && (time - lastPacketTime) > 10L * keepAlivePeriod) {
            setAuthenticated(false);
            Webcam.getLogger().warn("Webcam server timed out");
        }
        if (!authenticated && (time - lastAuthPacketSent) > 5000) {
            if (++authAttempts <= 5) {
                send(new AuthPacket(getPlayerUuid(), getSecret()));
                lastAuthPacketSent = time;
                Webcam.getLogger().info("Attempting to " + (wasAuthenticated ? "reauthenticate" : "authenticate") + " to the Webcam server"
                        + (wasAuthenticated || authAttempts > 1 ? ". Attempt #" + authAttempts : ""));
            } else {
                Webcam.getLogger().warn("Could not authenticate to Webcam server");
                shutdown();
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        lastPacketTime = System.currentTimeMillis();
        switch (packet) {
            case AuthAckPacket ignored -> {
                setAuthenticated(true);
                Webcam.getLogger().info("Successfully authenticated to server");
                send(new ShowWebcamsC2SPacket(WebcamModClient.CONFIG.getData().showWebcams()));
            }
            case PermissionsS2CPacket(boolean broadcast, boolean view) -> {
                broadcastPermission = broadcast;
                boolean prevView = viewPermission;
                viewPermission = view;
                if (prevView && !view) {
                    clearDisplayingVideos();
                }
            }
            case ServerConfigPacket(SyncedServerConfig config) ->
                    serverConfig = config;
            case KeepAlivePacket ignored ->
                    send(KeepAlivePacket.INSTANCE);
            case VideoS2CPacket(VideoSource source, NalUnit nal) -> {
                if (viewPermission) {
                    if (WebcamModClient.CONFIG.getData().showWebcams()) {
                        if (!WebcamModClient.BLOCKED_SOURCES.getData().contains(source.getUuid())) {
                            DisplayingVideo displayingVideo = displayingVideos.computeIfAbsent(source.getUuid(), DisplayingVideo::new);
                            displayingVideo.onVideoPacket(source, nal);
                        } else {
                            send(new AddBlockedSourceC2SPacket(source.getUuid()));
                        }
                    } else {
                        send(new ShowWebcamsC2SPacket(false));
                    }
                }
            }
            case CloseSourceS2CPacket(UUID sourceUuid) ->
                    Minecraft.getInstance().execute(() -> {
                        DisplayingVideo displayingVideo = displayingVideos.remove(sourceUuid);
                        if (displayingVideo != null) {
                            displayingVideo.close();
                        }
                    }
            );
            case KnownSourcesS2CPacket(List<KnownSource> sources) -> {
                for (KnownSource source : sources) {
                    KnownSourceClient prev = knownSources.put(source.getUuid(), new KnownSourceClient(source));
                    if (prev != null) {
                        prev.close();
                    }
                }
            }
            default -> throw new IllegalStateException("Can't handle packet " + packet + " from server");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Webcam.isDebugMode()) {
            Webcam.getLogger().warn("Error processing packet on client", cause);
        }
    }

    @Override
    public int getSelectedDevice() {
        return WebcamModClient.CONFIG.getData().selectedDevice();
    }

    @Override
    @Nullable
    public Resolution getResolution() {
        return WebcamModClient.CONFIG.getData().webcamResolution();
    }

    @Override
    public int getFps() {
        return WebcamModClient.CONFIG.getData().webcamFps();
    }

    @Override
    public int getImageDimension() {
        return serverConfig.imageDimension();
    }

    @Override
    public boolean isListeningFrames() {
        return broadcastPermission && WebcamModClient.CONFIG.getData().webcamEnabled();
    }

    @Override
    public void onFrame(int deviceNumber, int fps, int width, int height, byte[] rgba) {
        if (isAuthenticated() && broadcastPermission) {
            ClientConfig clientConfig = WebcamModClient.CONFIG.getData();
            if (deviceNumber == clientConfig.selectedDevice()) {
                SyncedServerConfig serverConfig = this.serverConfig;
                int mtu = serverConfig.mtu();
                int bitrate = Math.min(serverConfig.bitrate(), clientConfig.maxBitrate());
                for (byte[] nal : h264Encoder.encode(fps, mtu, bitrate, width, height, rgba)) {
                    sendBatching(new VideoC2SPacket(new NalUnit(nalSequenceNumber++, nal)));
                }
                flushChannel();
            }
        }
    }

    @Override
    public void onError(DeviceException e) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.empty().append(e.getText()).withStyle(ChatFormatting.RED), true);
            }
        });
    }

}
