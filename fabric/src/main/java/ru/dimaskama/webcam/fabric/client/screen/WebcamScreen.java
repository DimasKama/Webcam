package ru.dimaskama.webcam.fabric.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.client.*;
import ru.dimaskama.webcam.fabric.client.config.Resolution;
import ru.dimaskama.webcam.fabric.client.config.ClientConfig;
import ru.dimaskama.webcam.fabric.client.net.WebcamClient;
import ru.dimaskama.webcam.fabric.client.screen.widget.*;
import ru.dimaskama.webcam.net.packet.CloseSourceC2SPacket;
import ru.dimaskama.webcam.net.packet.ShowWebcamsC2SPacket;

import javax.annotation.Nullable;

public class WebcamScreen extends Screen implements WebcamOutputListener {

    public static final ResourceLocation BACKGROUND_SPRITE = WebcamFabric.id("background");
    private static final ResourceLocation PREVIEW_TEXTURE = WebcamFabric.id("webcam_preview");
    private static final int BUTTON_WIDTH = 152;
    private static final int MENU_HEIGHT = 167;
    private static final int PREVIEW_DIM = 142;
    private static boolean showPreview = true;
    private static int previewTextureDeviceIndex;
    private static DynamicTexture previewTexture;
    private final Screen parent;
    private final boolean notFromGame;
    private boolean firstInit = true;
    private boolean addedToWebcamListeners;
    private boolean configDirty;

    private int selectedDevice;
    private Resolution resolution;
    private int fps;
    private boolean showIcons;

    private int menuWidth;
    private int menuX, menuY;
    private Component errorMessage;
    private long errorMessageTime;

    public WebcamScreen(Screen parent) {
        this(parent, true);
    }

    public WebcamScreen(Screen parent, boolean notFromGame) {
        super(Component.translatable("webcam.screen.webcam"));
        this.parent = parent;
        this.notFromGame = notFromGame;
    }

    @Override
    public int getSelectedDevice() {
        return selectedDevice;
    }

    @Override
    @Nullable
    public Resolution getResolution() {
        return resolution;
    }

    @Override
    public int getFps() {
        return fps;
    }

    @Override
    public int getImageDimension() {
        return -1;
    }

    @Override
    public boolean isListeningFrames() {
        return showPreview || WebcamFabricClient.CONFIG.getData().webcamEnabled();
    }

    @Override
    public void onFrame(int deviceNumber, int fps, int width, int height, byte[] rgba) {
        Minecraft.getInstance().execute(() -> {
            try {
                if (deviceNumber == selectedDevice) {
                    previewTextureDeviceIndex = selectedDevice;
                    NativeImage prevImage = previewTexture != null ? previewTexture.getPixels() : null;
                    NativeImage newImage = ImageUtil.createNativeImage(prevImage, width, height, rgba);
                    if (newImage != prevImage) {
                        previewTexture = new DynamicTexture(newImage);
                        previewTexture.setFilter(true, false);
                        minecraft.getTextureManager().register(PREVIEW_TEXTURE, previewTexture);
                    } else {
                        previewTexture.upload();
                    }
                }
            } catch (Exception e) {
                if (Webcam.isDebugMode()) {
                    Webcam.getLogger().warn("Preview error", e);
                }
            }
        });
    }

    @Override
    public void onError(DeviceException e) {
        errorMessage = e.getText();
        errorMessageTime = System.currentTimeMillis();
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    protected void init() {
        if (!addedToWebcamListeners) {
            addedToWebcamListeners = true;
            Webcams.addListener(this);
        }
        ClientConfig config = WebcamFabricClient.CONFIG.getData();
        if (firstInit) {
            firstInit = false;
            selectedDevice = config.selectedDevice();
            resolution = config.webcamResolution();
            fps = config.webcamFps();
            showIcons = config.showIcons();
        }
        menuWidth = BUTTON_WIDTH + 8 + (showPreview ? PREVIEW_DIM + 2 : 0);
        menuX = (width - menuWidth) >> 1;
        menuY = (height - MENU_HEIGHT) >> 1;
        int buttonY = menuY + 21;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                BUTTON_WIDTH, 16,
                "webcam.screen.webcam.show_webcams",
                config.showWebcams(),
                b -> updateAndSetConfig(WebcamFabricClient.CONFIG.getData().withShowWebcams(b))
        ));
        buttonY += 18;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                BUTTON_WIDTH, 16,
                "webcam.screen.webcam.webcam_enabled",
                config.webcamEnabled(),
                b -> updateAndSetConfig(WebcamFabricClient.CONFIG.getData().withWebcamEnabled(b))
        ));
        buttonY += 18;
        addRenderableWidget(new DeviceSelectButton(
                menuX + 4, buttonY,
                BUTTON_WIDTH - 17, 16,
                selectedDevice,
                i -> selectedDevice = i
        ));
        addRenderableWidget(new UpdateDevicesButton(
                menuX + BUTTON_WIDTH - 12, buttonY,
                16, 16
        ));
        buttonY += 18;
        addRenderableWidget(new ResolutionSelectButton(
                menuX + 4, buttonY,
                BUTTON_WIDTH, 16,
                resolution,
                r -> resolution = r
        ));
        buttonY += 18;
        addRenderableWidget(new ClampedIntSlider(
                menuX + 4, buttonY,
                BUTTON_WIDTH, 16,
                "webcam.screen.webcam.fps",
                ClientConfig.MIN_FPS, ClientConfig.MAX_FPS,
                fps,
                i -> fps = i
        )).setTooltip(Tooltip.create(Component.translatable("webcam.screen.webcam.fps.tooltip")));
        buttonY += 18;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                BUTTON_WIDTH, 16,
                "webcam.screen.webcam.show_icons",
                showIcons,
                b -> showIcons = b
        ));
        buttonY += 18;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                BUTTON_WIDTH, 16,
                "webcam.screen.webcam.show_preview",
                showPreview,
                b -> {
                    showPreview = b;
                    rebuildWidgets();
                }
        ));
        buttonY += 18;
        Button advanced = addRenderableWidget(Button.builder(
                Component.translatable("webcam.screen.webcam.advanced"),
                button -> {
                    if (parent != null && notFromGame) {
                        minecraft.setScreen(parent);
                    } else {
                        minecraft.setScreen(new AdvancedWebcamScreen().create(this));
                    }
                }
        ).bounds(menuX + 4, buttonY, BUTTON_WIDTH, 16).build());
        if (!AdvancedWebcamScreen.CAN_USE) {
            advanced.setTooltip(Tooltip.create(Component.translatable("webcam.screen.webcam.advanced.not_available")));
            advanced.active = false;
        }

        addRenderableWidget(new StatusWidget(menuX + 4, menuY + 4, 16, 16));
        addRenderableWidget(new PlayersWebcamsButton(menuX + menuWidth - 20, menuY + 4, 16, 16));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, menuX, menuY, menuWidth, MENU_HEIGHT);
        guiGraphics.drawString(font, title, (width - font.width(title)) >> 1, menuY + 7, 0xFF555555, false);
        if (errorMessage != null) {
            if (System.currentTimeMillis() - errorMessageTime <= 4000L) {
                guiGraphics.drawCenteredString(font, errorMessage, width >> 1, menuY + MENU_HEIGHT + 10, 0xFFFF5555);
            }
        }
        if (showPreview && previewTexture != null && selectedDevice == previewTextureDeviceIndex && Webcams.isCapturing(selectedDevice)) {
            guiGraphics.blit(RenderType::guiTextured, PREVIEW_TEXTURE, menuX + menuWidth - PREVIEW_DIM - 4, menuY + 21, 0.0F, 0.0F, PREVIEW_DIM, PREVIEW_DIM, PREVIEW_DIM, PREVIEW_DIM);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return notFromGame;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        ClientConfig oldConfig = WebcamFabricClient.CONFIG.getData();
        ClientConfig newConfig = new ClientConfig(
                oldConfig.showWebcams(),
                oldConfig.webcamEnabled(),
                selectedDevice,
                resolution,
                fps,
                showIcons,
                oldConfig.maxDevices(),
                oldConfig.packetBufferSize(),
                oldConfig.maxBitrate(),
                oldConfig.hud()
        );
        if (configDirty || !newConfig.equals(oldConfig)) {
            configDirty = false;
            updateConfig(oldConfig, newConfig);
            WebcamFabricClient.CONFIG.setData(newConfig);
            WebcamFabricClient.CONFIG.save();
        }
        Webcams.removeListener(this);
        addedToWebcamListeners = false;
    }

    private void updateConfig(ClientConfig oldConfig, ClientConfig newConfig) {
        if (oldConfig.webcamEnabled() && !newConfig.webcamEnabled()) {
            WebcamClient webcamClient = WebcamClient.getInstance();
            if (webcamClient != null && webcamClient.isAuthenticated()) {
                webcamClient.send(CloseSourceC2SPacket.INSTANCE);
            }
        }
        if (oldConfig.showWebcams() != newConfig.showWebcams()) {
            WebcamClient webcamClient = WebcamClient.getInstance();
            if (webcamClient != null && webcamClient.isAuthenticated()) {
                webcamClient.send(new ShowWebcamsC2SPacket(newConfig.showWebcams()));
            }
        }
    }

    private void updateAndSetConfig(ClientConfig newConfig) {
        ClientConfig oldConfig = WebcamFabricClient.CONFIG.getData();
        if (!oldConfig.equals(newConfig)) {
            WebcamFabricClient.CONFIG.setData(newConfig);
            configDirty = true;
            updateConfig(oldConfig, newConfig);
        }
    }

}
