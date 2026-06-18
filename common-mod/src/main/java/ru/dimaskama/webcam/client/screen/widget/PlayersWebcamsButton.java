package ru.dimaskama.webcam.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.screen.PlayersWebcamsScreen;

public class PlayersWebcamsButton extends AbstractButton {

    private static final Identifier SPRITE = WebcamMod.id("button/manage_players_webcams");

    public PlayersWebcamsButton(int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("webcam.screen.webcam.manage_players_webcams"));
        setTooltip(Tooltip.create(getMessage()));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractDefaultSprite(guiGraphics);
        int spriteX = getX() + ((getWidth() - 12) >> 1);
        int spriteY = getY() + ((getHeight() - 12) >> 1);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, spriteX, spriteY, 12, 12, 0xFFFFFFFF);
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen parent = minecraft.gui.screen();
        minecraft.gui.setScreen(new PlayersWebcamsScreen(parent, parent != null && parent.isPauseScreen()));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
