package ru.dimaskama.webcam.client.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.config.ClientConfig;
import ru.dimaskama.webcam.client.net.WebcamClient;
import ru.dimaskama.webcam.client.Webcams;

public class WebcamHud {

    private static final ResourceLocation WEBCAM_SPRITE = WebcamMod.id("webcam");
    private static final ResourceLocation WEBCAM_DISABLED_SPRITE = WebcamMod.id("webcam_disabled");
    private static final ResourceLocation WEBCAM_NO_CONNECTION_SPRITE = WebcamMod.id("webcam_no_connection");
    private static final ResourceLocation WEBCAM_CONNECTING_SPRITE = WebcamMod.id("webcam_connecting");

    public static void drawHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ClientConfig config = WebcamModClient.CONFIG.getData();
        if (config.showIcons()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(
                    config.hud().iconX() >= 0.0F ? config.hud().iconX() : (guiGraphics.guiWidth() + config.hud().iconX() - 16.0F * config.hud().iconScale()),
                    config.hud().iconY() >= 0.0F ? config.hud().iconY() : (guiGraphics.guiHeight() + config.hud().iconY() - 16.0F * config.hud().iconScale())
            );
            guiGraphics.pose().scale(config.hud().iconScale());
            WebcamClient client = WebcamClient.getInstance();
            ResourceLocation sprite;
            if (client == null) {
                sprite = WEBCAM_NO_CONNECTION_SPRITE;
            } else if (!client.isAuthenticated()) {
                sprite = WEBCAM_CONNECTING_SPRITE;
            } else if (client.isListeningFrames() && Webcams.isCapturing(config.selectedDevice())) {
                sprite = WEBCAM_SPRITE;
            } else {
                sprite = WEBCAM_DISABLED_SPRITE;
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, 16, 16);
            guiGraphics.pose().popMatrix();
        }
    }

}
