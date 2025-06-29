package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.client.WebcamFabricClient;
import ru.dimaskama.webcam.fabric.client.Webcams;

public class WebcamHud {

    public static final boolean IS_VOICECHAT_LOADED = FabricLoader.getInstance().isModLoaded("voicechat");
    private static final ResourceLocation WEBCAM_SPRITE = WebcamFabric.id("webcam");
    private static final ResourceLocation WEBCAM_DISABLED_SPRITE = WebcamFabric.id("webcam_disabled");

    public static void drawHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (WebcamFabricClient.CONFIG.getData().showIcons()) {
            Window window = Minecraft.getInstance().getWindow();
            int height = window.getGuiScaledHeight();
            ResourceLocation sprite = Webcams.isCapturing() ? WEBCAM_SPRITE : WEBCAM_DISABLED_SPRITE;
            guiGraphics.blitSprite(RenderType::guiTextured, sprite, IS_VOICECHAT_LOADED ? 32 : 16, height - 32, 16, 16);
        }
    }

}
