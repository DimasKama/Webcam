package ru.dimaskama.webcam.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record ClientConfig(
        boolean showWebcams,
        boolean webcamEnabled,
        int selectedDevice,
        Resolution webcamResolution,
        int webcamFps,
        boolean showIcons,
        int maxDevices,
        int packetBufferSize,
        int maxBitrate,
        HudSettings hud
) {

    public static final int MIN_FPS = 5;
    public static final int MAX_FPS = 60;
    public static final int MIN_MAX_DEVICES = 1;
    public static final int MAX_MAX_DEVICES = 50;
    public static final int DEFAULT_MAX_DEVICES = 5;
    public static final int MIN_PACKET_BUFFER_SIZE = 1;
    public static final int MAX_PACKET_BUFFER_SIZE = 100;
    public static final int DEFAULT_PACKET_BUFFER_SIZE = 50;
    public static final int MIN_MAX_BITRATE = 10;
    public static final int MAX_MAX_BITRATE = 10_000;
    public static final int DEFAULT_MAX_BITRATE = 5_000;
    public static final Codec<ClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.BOOL, "show_webcams", () -> true).forGetter(ClientConfig::showWebcams),
            defaultedField(Codec.BOOL, "webcam_enabled", () -> false).forGetter(ClientConfig::webcamEnabled),
            defaultedField(Codec.INT, "selected_device", () -> 0).forGetter(ClientConfig::selectedDevice),
            defaultedField(Resolution.CODEC, "webcam_resolution", () -> Resolution.R_1280X720).forGetter(ClientConfig::webcamResolution),
            defaultedField(Codec.intRange(MIN_FPS, MAX_FPS), "webcam_fps", () -> 30).forGetter(ClientConfig::webcamFps),
            defaultedField(Codec.BOOL, "show_icons", () -> true).forGetter(ClientConfig::showIcons),
            defaultedField(Codec.intRange(MIN_MAX_DEVICES, MAX_MAX_DEVICES), "max_devices", () -> DEFAULT_MAX_DEVICES).forGetter(ClientConfig::maxDevices),
            defaultedField(Codec.intRange(MIN_PACKET_BUFFER_SIZE, MAX_PACKET_BUFFER_SIZE), "packet_buffer_size", () -> DEFAULT_PACKET_BUFFER_SIZE).forGetter(ClientConfig::packetBufferSize),
            defaultedField(Codec.intRange(MIN_MAX_BITRATE, MAX_MAX_BITRATE), "max_bitrate", () -> DEFAULT_MAX_BITRATE).forGetter(ClientConfig::maxBitrate),
            defaultedField(HudSettings.CODEC, "hud", HudSettings::new).forGetter(ClientConfig::hud)
    ).apply(instance, ClientConfig::new));

    public ClientConfig() {
        this(true, false, 0, Resolution.R_1280X720, 30, true, DEFAULT_MAX_DEVICES, DEFAULT_PACKET_BUFFER_SIZE, DEFAULT_MAX_BITRATE, new HudSettings());
    }

    public ClientConfig withShowWebcams(boolean showWebcams) {
        return new ClientConfig(showWebcams, webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices, packetBufferSize, maxBitrate, hud);
    }

    public ClientConfig withWebcamEnabled(boolean webcamEnabled) {
        return new ClientConfig(showWebcams, webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices, packetBufferSize, maxBitrate, hud);
    }

}
