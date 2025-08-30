package ru.dimaskama.webcam.client.fabric.screen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.config.ClientConfig;
import ru.dimaskama.webcam.client.config.HudSettings;
import ru.dimaskama.webcam.client.screen.WebcamScreen;

import javax.annotation.Nullable;

public class AdvancedWebcamScreen {

    private static final Component MAIN_SETTINGS = Component.translatable("webcam.screen.advanced.main_settings");
    private int maxDevices;
    private int packetBufferSize;
    private int maxBitrate;
    private HudSettings hud;

    public AdvancedWebcamScreen() {
        ClientConfig config = WebcamModClient.CONFIG.getData();
        maxDevices = config.maxDevices();
        packetBufferSize = config.packetBufferSize();
        maxBitrate = config.maxBitrate();
        hud = config.hud();
    }

    public Screen create(@Nullable Screen parent) {
        assertCanUse();
        return new Internal().create(parent);
    }

    public static void tick(Minecraft minecraft) {
        assertCanUse();
        Internal.tick(minecraft);
    }

    private static void assertCanUse() {
        if (!WebcamModClient.canUseAdvancedConfigScreen()) {
            throw new IllegalStateException("Cloth Config is not loaded");
        }
    }

    private class Internal {

        public Screen create(@Nullable Screen parent) {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("webcam.screen.advanced"))
                    .setSavingRunnable(() -> {
                        ClientConfig oldConfig = WebcamModClient.CONFIG.getData();
                        ClientConfig newConfig = new ClientConfig(
                                oldConfig.showWebcams(),
                                oldConfig.webcamEnabled(),
                                oldConfig.selectedDevice(),
                                oldConfig.webcamResolution(),
                                oldConfig.webcamFps(),
                                oldConfig.showIcons(),
                                maxDevices,
                                packetBufferSize,
                                maxBitrate,
                                hud
                        );
                        if (!newConfig.equals(oldConfig)) {
                            WebcamModClient.CONFIG.setData(newConfig);
                            WebcamModClient.CONFIG.save();
                        }
                    });
            builder.getOrCreateCategory(Component.translatable("webcam.screen.advanced.general"))
                    .addEntry(builder.entryBuilder()
                            .startIntField(Component.translatable("webcam.screen.advanced.general.max_devices"), maxDevices)
                            .setTooltip(Component.translatable("webcam.screen.advanced.general.max_devices.description"))
                            .setDefaultValue(ClientConfig.DEFAULT_MAX_DEVICES)
                            .setMin(ClientConfig.MIN_MAX_DEVICES)
                            .setMax(ClientConfig.MAX_MAX_DEVICES)
                            .setSaveConsumer(i -> maxDevices = i)
                            .build())
                    .addEntry(builder.entryBuilder()
                            .startIntField(Component.translatable("webcam.screen.advanced.general.packet_buffer_size"), packetBufferSize)
                            .setTooltip(Component.translatable("webcam.screen.advanced.general.packet_buffer_size.description"))
                            .setDefaultValue(ClientConfig.DEFAULT_PACKET_BUFFER_SIZE)
                            .setMin(ClientConfig.MIN_PACKET_BUFFER_SIZE)
                            .setMax(ClientConfig.MAX_PACKET_BUFFER_SIZE)
                            .setSaveConsumer(i -> packetBufferSize = i)
                            .build())
                    .addEntry(builder.entryBuilder()
                            .startIntField(Component.translatable("webcam.screen.advanced.general.max_bitrate"), maxBitrate)
                            .setTooltip(Component.translatable("webcam.screen.advanced.general.max_bitrate.description"))
                            .setDefaultValue(ClientConfig.DEFAULT_MAX_BITRATE)
                            .setMin(ClientConfig.MIN_MAX_BITRATE)
                            .setMax(ClientConfig.MAX_MAX_BITRATE)
                            .setSaveConsumer(i -> maxBitrate = i)
                            .build());
            builder.getOrCreateCategory(Component.translatable("webcam.screen.advanced.hud"))
                    .addEntry(builder.entryBuilder()
                            .startFloatField(Component.translatable("webcam.screen.advanced.hud.icon_x"), hud.iconX())
                            .setTooltip(Component.translatable("webcam.screen.advanced.hud.icon_x.description"))
                            .setDefaultValue(HudSettings.DEFAULT_ICON_X)
                            .setSaveConsumer(i -> hud = hud.withIconX(i))
                            .build())
                    .addEntry(builder.entryBuilder()
                            .startFloatField(Component.translatable("webcam.screen.advanced.hud.icon_y"), hud.iconY())
                            .setTooltip(Component.translatable("webcam.screen.advanced.hud.icon_y.description"))
                            .setDefaultValue(HudSettings.DEFAULT_ICON_Y)
                            .setSaveConsumer(i -> hud = hud.withIconY(i))
                            .build())
                    .addEntry(builder.entryBuilder()
                            .startFloatField(Component.translatable("webcam.screen.advanced.hud.icon_scale"), hud.iconScale())
                            .setDefaultValue(HudSettings.DEFAULT_ICON_SCALE)
                            .setMin(0.0F)
                            .setSaveConsumer(f -> hud = hud.withIconScale(f))
                            .build());
            if (!(parent instanceof WebcamScreen)) {
                builder.getOrCreateCategory(MAIN_SETTINGS);
            }
            return builder.build();
        }

        public static void tick(Minecraft minecraft) {
            if (minecraft.screen instanceof ClothConfigScreen screen) {
                if (screen.getSelectedCategory().equals(MAIN_SETTINGS)) {
                    screen.selectedCategoryIndex = 0;
                    minecraft.setScreen(new WebcamScreen(screen, true));
                }
            }
        }

    }

}
