package ru.dimaskama.webcam.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record ServerConfig(
        int port,
        String bindAddress,
        String host,
        int keepAlivePeriod,
        int permissionCheckPeriod,
        double maxDisplayDistance,
        boolean displayOnFace,
        VideoDisplayShape displayShape,
        float displayOffsetY,
        float displaySize,
        boolean hideNicknames,
        boolean displaySelfWebcam,
        SyncedServerConfig synced,
        MessagesConfig messages
) {

    public static final double MIN_MAX_DISPLAY_DISTANCE = 5.0;
    public static final double MAX_MAX_DISPLAY_DISTANCE = 100000.0;
    public static final float MIN_DISPLAY_OFFSET_Y = -10.0F;
    public static final float MAX_DISPLAY_OFFSET_Y = 100.0F;
    public static final float MIN_DISPLAY_SIZE = 0.1F;
    public static final float MAX_DISPLAY_SIZE = 200.0F;
    private static final int DEFAULT_PORT = 25454;
    public static final Codec<ServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.intRange(1, 65535), "port", () -> DEFAULT_PORT).forGetter(ServerConfig::port),
            defaultedField(Codec.STRING, "bind_address", () -> "").forGetter(ServerConfig::bindAddress),
            defaultedField(Codec.STRING, "host", () -> "").forGetter(ServerConfig::host),
            defaultedField(Codec.intRange(100, 100_000), "keep_alive_period", () -> 1000).forGetter(ServerConfig::keepAlivePeriod),
            defaultedField(Codec.intRange(1, 100_000), "permission_check_period", () -> 60).forGetter(ServerConfig::permissionCheckPeriod),
            defaultedField(Codec.doubleRange(MIN_MAX_DISPLAY_DISTANCE, MAX_MAX_DISPLAY_DISTANCE), "max_display_distance", () -> 100.0).forGetter(ServerConfig::maxDisplayDistance),
            defaultedField(Codec.BOOL, "display_on_face", () -> false).forGetter(ServerConfig::displayOnFace),
            defaultedField(VideoDisplayShape.CODEC, "display_shape", () -> VideoDisplayShape.ROUND).forGetter(ServerConfig::displayShape),
            defaultedField(Codec.floatRange(MIN_DISPLAY_OFFSET_Y, MAX_DISPLAY_OFFSET_Y), "display_offset_y", () -> 1.3F).forGetter(ServerConfig::displayOffsetY),
            defaultedField(Codec.floatRange(MIN_DISPLAY_SIZE, MAX_DISPLAY_SIZE), "display_size", () -> 1.2F).forGetter(ServerConfig::displaySize),
            defaultedField(Codec.BOOL, "hide_nicknames", () -> true).forGetter(ServerConfig::hideNicknames),
            defaultedField(Codec.BOOL, "display_self_webcam", () -> false).forGetter(ServerConfig::displaySelfWebcam),
            defaultedField(SyncedServerConfig.CODEC, "synced", SyncedServerConfig::new).forGetter(ServerConfig::synced),
            defaultedField(MessagesConfig.CODEC, "messages", MessagesConfig::new).forGetter(ServerConfig::messages)
    ).apply(instance, ServerConfig::new));

    public ServerConfig() {
        this(DEFAULT_PORT, "", "", 1000, 60, 100.0, false, VideoDisplayShape.ROUND, 1.3F, 1.2F, true, false, new SyncedServerConfig(), new MessagesConfig());
    }

    public ServerConfig withPort(int port) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withBindAddress(String bindAddress) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withHost(String host) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withKeepAlivePeriod(int keepAlivePeriod) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withPermissionCheckPeriod(int permissionCheckPeriod) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withMaxDisplayDistance(double maxDisplayDistance) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withDisplayOnFace(boolean displayOnFace) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withDisplayShape(VideoDisplayShape displayShape) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withDisplayOffsetY(float displayOffsetY) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withDisplaySize(float displaySize) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withHideNicknames(boolean hideNicknames) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withDisplaySelfWebcam(boolean displaySelfWebcam) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

    public ServerConfig withSynced(SyncedServerConfig synced) {
        return new ServerConfig(port, bindAddress, host, keepAlivePeriod, permissionCheckPeriod, maxDisplayDistance, displayOnFace, displayShape, displayOffsetY, displaySize, hideNicknames, displaySelfWebcam, synced, messages);
    }

}
