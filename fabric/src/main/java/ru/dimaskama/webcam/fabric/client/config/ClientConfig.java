package ru.dimaskama.webcam.fabric.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record ClientConfig(
        boolean webcamEnabled,
        int selectedDevice,
        Resolution webcamResolution,
        int webcamFps,
        boolean showIcons,
        int maxDevices
) {

    public static final int MIN_FPS = 5;
    public static final int MAX_FPS = 60;
    public static final Codec<ClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.BOOL, "webcam_enabled", () -> true).forGetter(ClientConfig::webcamEnabled),
            defaultedField(Codec.INT, "selected_device", () -> 0).forGetter(ClientConfig::selectedDevice),
            defaultedField(Resolution.CODEC, "webcam_resolution", () -> Resolution.R_1280X720).forGetter(ClientConfig::webcamResolution),
            defaultedField(Codec.intRange(MIN_FPS, MAX_FPS), "webcam_fps", () -> 30).forGetter(ClientConfig::webcamFps),
            defaultedField(Codec.BOOL, "show_icons", () -> true).forGetter(ClientConfig::showIcons),
            defaultedField(Codec.intRange(1, 50), "max_devices", () -> 5).forGetter(ClientConfig::maxDevices)
    ).apply(instance, ClientConfig::new));

    public ClientConfig() {
        this(false, 0, Resolution.R_1280X720, 30, true, 5);
    }

    public ClientConfig withWebcamEnabled(boolean webcamEnabled) {
        return new ClientConfig(webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices);
    }

    public ClientConfig withSelectedDevice(int selectedDevice) {
        return new ClientConfig(webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices);
    }

    public ClientConfig withWebcamResolution(Resolution webcamResolution) {
        return new ClientConfig(webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices);
    }

    public ClientConfig withWebcamFps(int webcamFps) {
        return new ClientConfig(webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices);
    }

    public ClientConfig withShowIcons(boolean showIcons) {
        return new ClientConfig(webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices);
    }

    public ClientConfig withMaxDevices(int maxDevices) {
        return new ClientConfig(webcamEnabled, selectedDevice, webcamResolution, webcamFps, showIcons, maxDevices);
    }

}
