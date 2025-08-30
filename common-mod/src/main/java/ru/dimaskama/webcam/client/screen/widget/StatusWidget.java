package ru.dimaskama.webcam.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.net.WebcamClient;

public class StatusWidget extends AbstractWidget {

    private static final ResourceLocation FINE_SPRITE = WebcamMod.id("fine");
    private static final ResourceLocation NOT_FINE_SPRITE = WebcamMod.id("not_fine");
    private Status status;

    public StatusWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        update();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        update();
        ResourceLocation sprite = status.fine ? FINE_SPRITE : NOT_FINE_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), getWidth(), getHeight());
    }

    private void update() {
        WebcamClient client = WebcamClient.getInstance();
        Status status = client != null ? client.isAuthenticated() ? Status.CONNECTED : Status.CONNECTING : Status.NO_CONNECTION;
        if (this.status != status) {
            this.status = status;
            setMessage(status.text);
            setTooltip(Tooltip.create(status.text));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    protected boolean isValidClickButton(int i) {
        return false;
    }

    private enum Status {

        NO_CONNECTION(Component.translatable("webcam.screen.webcam.status.no_connection"), false),
        CONNECTING(Component.translatable("webcam.screen.webcam.status.connecting"), false),
        CONNECTED(Component.translatable("webcam.screen.webcam.status.connected"), true);

        private final Component text;
        private final boolean fine;

        Status(Component text, boolean fine) {
            this.text = text;
            this.fine = fine;
        }
    }

}
