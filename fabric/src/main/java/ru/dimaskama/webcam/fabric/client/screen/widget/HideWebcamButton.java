package ru.dimaskama.webcam.fabric.client.screen.widget;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;

public class HideWebcamButton extends AbstractButton {

    private static final ResourceLocation BLOCKED_SPRITE = WebcamFabric.id("webcam_disabled");
    private static final ResourceLocation NOT_BLOCKED_SPRITE = WebcamFabric.id("webcam");
    private final BooleanConsumer consumer;
    private boolean blocked;

    public HideWebcamButton(boolean blocked, BooleanConsumer consumer) {
        super(-999, -999, 0, 0, Component.empty());
        this.consumer = consumer;
        this.blocked = blocked;
        update();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        int spriteX = getX() + ((getWidth() - 16) >> 1);
        int spriteY = getY() + ((getHeight() - 16) >> 1);
        guiGraphics.blitSprite(RenderType::guiTextured, blocked ? BLOCKED_SPRITE : NOT_BLOCKED_SPRITE, spriteX, spriteY, 16, 16, 0xFFFFFFFF);
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int i) {
    }

    @Override
    public void onPress() {
        blocked = !blocked;
        update();
        consumer.accept(blocked);
    }

    private void update() {
        setMessage(blocked
                ? Component.translatable("webcam.screen.players_webcams.show")
                : Component.translatable("webcam.screen.players_webcams.hide"));
        setTooltip(Tooltip.create(getMessage()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
