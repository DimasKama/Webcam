package ru.dimaskama.webcam.client.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import ru.dimaskama.webcam.client.config.Resolution;

import java.util.function.Consumer;

public class ResolutionSelectButton extends AbstractButton {

    private final Consumer<Resolution> updateConsumer;
    private Resolution selected;

    public ResolutionSelectButton(int x, int y, int width, int height, Resolution selected, Consumer<Resolution> updateConsumer) {
        super(x, y, width, height, getText(selected));
        this.updateConsumer = updateConsumer;
        this.selected = selected;
        setTooltip(Tooltip.create(Component.translatable("webcam.screen.webcam.resolution.tooltip")));
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        Resolution[] available = Resolution.values();
        int index = 0;
        for (int i = 0; i < available.length; i++) {
            if (available[i] == selected) {
                index = (i + 1) % available.length;
            }
        }
        selected = available[index];
        setMessage(getText(selected));
        updateConsumer.accept(selected);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        extractDefaultSprite(guiGraphics);
        extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_ONLY));
    }

    private static Component getText(Resolution selected) {
        return Component.translatable("webcam.screen.webcam.resolution", selected.key + " (" + selected.aspectRatio.key + ")");
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
