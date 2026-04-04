package ru.dimaskama.webcam.client.screen.widget;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class OnOffButton extends AbstractButton {

    private final String translatableKey;
    private final BooleanConsumer consumer;
    private boolean value;

    public OnOffButton(int x, int y, int width, int height, String translatableKey, boolean value, BooleanConsumer consumer) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.translatableKey = translatableKey;
        this.consumer = consumer;
        this.value = value;
        updateMessage();
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        value = !value;
        updateMessage();
        consumer.accept(value);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractDefaultSprite(guiGraphics);
        extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_ONLY));
    }

    private void updateMessage() {
        setMessage(Component.translatable(translatableKey, value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
