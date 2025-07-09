package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.client.WebcamClient;
import ru.dimaskama.webcam.fabric.client.WebcamFabricClient;
import ru.dimaskama.webcam.fabric.client.Webcams;

public class WebcamHud {

    public static final boolean IS_VOICECHAT_LOADED = FabricLoader.getInstance().isModLoaded("voicechat");
    private static final ResourceLocation WEBCAM_SPRITE = WebcamFabric.id("webcam");
    private static final ResourceLocation WEBCAM_DISABLED_SPRITE = WebcamFabric.id("webcam_disabled");
    private static final ResourceLocation WEBCAM_NO_CONNECTION_SPRITE = WebcamFabric.id("webcam_no_connection");
    private static final ResourceLocation WEBCAM_CONNECTING_SPRITE = WebcamFabric.id("webcam_connecting");

    public static void drawHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (WebcamFabricClient.CONFIG.getData().showIcons()) {
            Window window = Minecraft.getInstance().getWindow();
            int height = window.getGuiScaledHeight();
            ResourceLocation sprite;
            if (Webcams.isCapturing()) {
                sprite = WEBCAM_SPRITE;
            } else {
                WebcamClient client = WebcamClient.getInstance();
                if (client == null || client.isClosed()) {
                    sprite = WEBCAM_NO_CONNECTION_SPRITE;
                } else if (!client.isAuthenticated()) {
                    sprite = WEBCAM_CONNECTING_SPRITE;
                } else {
                    sprite = WEBCAM_DISABLED_SPRITE;
                }
            }
            guiGraphics.blitSprite(RenderType::guiTextured, sprite, IS_VOICECHAT_LOADED ? 32 : 16, height - 32, 16, 16);
        }
    }

}
