package ru.dimaskama.webcam.fabric.client.screen.widget;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.client.Webcams;

import java.util.concurrent.CompletableFuture;

public class UpdateDevicesButton extends AbstractButton {

    private static final ResourceLocation SPRITE = WebcamFabric.id("button/update");
    private static CompletableFuture<Void> updateFuture = CompletableFuture.completedFuture(null);

    public UpdateDevicesButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("webcam.screen.webcam.update_devices"));
        updateActive(true);
    }

    public static void updateDevices() {
        updateFuture.join();
        updateFuture = CompletableFuture.runAsync(Webcams::updateDevices, Util.backgroundExecutor());
    }

    @Override
    public void onPress() {
        updateDevices();
    }

    private void updateActive(boolean updateTooltip) {
        boolean prevActive = active;
        active = updateFuture.isDone();
        if (updateTooltip || prevActive != active) {
            setTooltip(active ? Tooltip.create(getMessage()) : null);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateActive(false);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        int spriteX = getX() + ((getWidth() - 16) >> 1);
        int spriteY = getY() + ((getHeight() - 16) >> 1);
        guiGraphics.blitSprite(RenderType::guiTextured, SPRITE, spriteX, spriteY, 16, 16, active ? 0xFFFFFFFF : 0xFF777777);
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int i) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
