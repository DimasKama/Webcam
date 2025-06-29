package ru.dimaskama.webcam.fabric.client.screen.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;

public class ClampedIntSlider extends AbstractSliderButton {

    private final String translatableKey;
    private final IntConsumer consumer;
    private final int min;
    private final int max;
    private int intValue;

    public ClampedIntSlider(int x, int y, int width, int height, String translatableKey, int min, int max, int intValue, IntConsumer consumer) {
        super(x, y, width, height, CommonComponents.EMPTY, Mth.inverseLerp((double) intValue, min, max));
        this.translatableKey = translatableKey;
        this.consumer = consumer;
        this.min = min;
        this.max = max;
        this.intValue = intValue;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.translatable(translatableKey, intValue));
    }

    @Override
    protected void applyValue() {
        intValue = Mth.lerpInt((float) value, min, max);
        consumer.accept(intValue);
    }

}
