package ru.dimaskama.webcam.fabric.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.client.screen.PlayersWebcamsScreen;

public class PlayersWebcamsButton extends AbstractButton {

    private static final ResourceLocation SPRITE = WebcamFabric.id("button/manage_players_webcams");

    public PlayersWebcamsButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("webcam.screen.webcam.manage_players_webcams"));
        setTooltip(Tooltip.create(getMessage()));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        int spriteX = getX() + ((getWidth() - 12) >> 1);
        int spriteY = getY() + ((getHeight() - 12) >> 1);
        guiGraphics.blitSprite(RenderType::guiTextured, SPRITE, spriteX, spriteY, 12, 12, 0xFFFFFFFF);
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int i) {
    }

    @Override
    public void onPress() {
        Minecraft minecraft = Minecraft.getInstance();
        Screen parent = minecraft.screen;
        minecraft.setScreen(new PlayersWebcamsScreen(parent, parent != null && parent.isPauseScreen()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
