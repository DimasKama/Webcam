package ru.dimaskama.webcam.fabric.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.client.ImageUtil;
import ru.dimaskama.webcam.fabric.client.WebcamClient;
import ru.dimaskama.webcam.fabric.client.WebcamFabricClient;
import ru.dimaskama.webcam.fabric.client.Webcams;
import ru.dimaskama.webcam.fabric.client.config.ClientConfig;
import ru.dimaskama.webcam.fabric.client.screen.widget.*;
import ru.dimaskama.webcam.net.packet.CloseSourceC2SPacket;

// This screen can be only opened if the client is connected to a Webcam server
public class WebcamScreen extends Screen {

    private static final ResourceLocation BACKGROUND_SPRITE = WebcamFabric.id("background");
    private static final ResourceLocation PREVIEW_TEXTURE = WebcamFabric.id("webcam_preview");
    private static final int MENU_WIDTH = 160;
    private static final int MENU_HEIGHT = 200;
    private static boolean showPreview;
    private static DynamicTexture previewTexture;
    private final Screen parent;
    private final ClientConfig initialConfig = WebcamFabricClient.CONFIG.getData();
    private ClientConfig config = initialConfig;
    private boolean firstInit = true;
    private int menuX, menuY;
    private int buttonsEndY;
    private Component errorMessage;
    private long errorMessageTime;

    public WebcamScreen(Screen parent) {
        super(Component.translatable("webcam.screen.webcam"));
        this.parent = parent;
    }

    public void setErrorMessage(Component errorMessage) {
        this.errorMessage = errorMessage;
        errorMessageTime = System.currentTimeMillis();
    }

    public void onFrame(byte[] jpgImage) {
        try {
            NativeImage prevImage = previewTexture != null ? previewTexture.getPixels() : null;
            NativeImage newImage = ImageUtil.convertJpgToNativeImage(prevImage, jpgImage);
            if (newImage != prevImage) {
                previewTexture = new DynamicTexture(PREVIEW_TEXTURE::getPath, newImage);
                previewTexture.setFilter(true, false);
                minecraft.getTextureManager().register(PREVIEW_TEXTURE, previewTexture);
            } else {
                previewTexture.upload();
            }
        } catch (Exception e) {
            if (Webcam.isDebugMode()) {
                Webcam.getLogger().warn("Preview error", e);
            }
        }
    }

    @Override
    protected void init() {
        if (checkClosed()) {
            return;
        }
        if (firstInit) {
            firstInit = false;
            updateCapturing();
        }
        menuX = (width - MENU_WIDTH) >> 1;
        menuY = (height - MENU_HEIGHT) >> 1;
        int buttonY = menuY + 20;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                MENU_WIDTH - 8, 16,
                "webcam.screen.webcam.webcam_enabled",
                config.webcamEnabled(),
                b -> updateConfig(config.withWebcamEnabled(b))
        ));
        buttonY += 18;
        addRenderableWidget(new DeviceSelectButton(
                menuX + 4, buttonY,
                MENU_WIDTH - 8 - 17, 16,
                config.selectedDevice(),
                s -> updateConfig(config.withSelectedDevice(s))
        ));
        addRenderableWidget(new UpdateDevicesButton(
                menuX + MENU_WIDTH - 4 - 16, buttonY,
                16, 16
        ));
        buttonY += 18;
        addRenderableWidget(new ResolutionSelectButton(
                menuX + 4, buttonY,
                MENU_WIDTH - 8, 16,
                config.webcamResolution(),
                r -> updateConfig(config.withWebcamResolution(r))
        ));
        buttonY += 18;
        addRenderableWidget(new ClampedIntSlider(
                menuX + 4, buttonY,
                MENU_WIDTH - 8, 16,
                "webcam.screen.webcam.fps",
                ClientConfig.MIN_FPS, ClientConfig.MAX_FPS,
                config.webcamFps(),
                i -> updateConfig(config.withWebcamFps(i))
        )).setTooltip(Tooltip.create(Component.translatable("webcam.screen.webcam.fps.tooltip")));
        buttonY += 18;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                MENU_WIDTH - 8, 16,
                "webcam.screen.webcam.show_icons",
                config.showIcons(),
                b -> updateConfig(config.withShowIcons(b))
        ));
        buttonY += 18;
        addRenderableWidget(new OnOffButton(
                menuX + 4, buttonY,
                MENU_WIDTH - 8, 16,
                "webcam.screen.webcam.show_preview",
                showPreview,
                b -> {
                    showPreview = b;
                    updateCapturing();
                }
        )).setTooltip(Tooltip.create(Component.translatable("webcam.screen.webcam.show_preview.tooltip")));
        buttonY += 18;
        buttonsEndY = buttonY;
    }

    @Override
    public void tick() {
        checkClosed();
    }

    private boolean checkClosed() {
        WebcamClient client = WebcamClient.getInstance();
        if (client == null || client.isClosed()) {
            onClose();
            return true;
        }
        return false;
    }

    private void updateConfig(ClientConfig config) {
        if (this.config.webcamEnabled() && !config.webcamEnabled()) {
            // Webcam disabled
            WebcamClient client = WebcamClient.getInstance();
            if (client != null && !client.isClosed() && client.isAuthenticated()) {
                client.send(CloseSourceC2SPacket.INSTANCE);
            }
        }
        this.config = config;
        WebcamFabricClient.CONFIG.setData(config);
        updateCapturing();
    }

    private void updateCapturing() {
        Webcams.updateCapturing(config.webcamEnabled() || showPreview, config.selectedDevice(), config.webcamResolution(), config.webcamFps());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
        guiGraphics.drawString(font, title, (width - font.width(title)) >> 1, menuY + 7, 0xFF555555, false);
        if (errorMessage != null) {
            if (System.currentTimeMillis() - errorMessageTime <= 4000L) {
                guiGraphics.drawCenteredString(font, errorMessage, width >> 1, menuY + MENU_HEIGHT + 10, 0xFFFF5555);
            }
        }
        if (showPreview && previewTexture != null && Webcams.isCapturing()) {
            // Show preview is ON and the first preview frame is loaded
            int previewY = buttonsEndY;
            int previewBottomY = menuY + MENU_HEIGHT - 4;
            int previewDim = previewBottomY - previewY;
            if (previewDim > 1) {
                int previewX = (width - previewDim) >> 1;
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, PREVIEW_TEXTURE, previewX, previewY, 0.0F, 0.0F, previewDim, previewDim, previewDim, previewDim);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        if (!initialConfig.equals(config)) {
            WebcamFabricClient.CONFIG.save();
        }
        Webcams.updateCapturing();
    }

}
