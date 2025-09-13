package ru.dimaskama.webcam.client.screen.widget;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import ru.dimaskama.webcam.client.cap.Capturing;

import java.util.function.IntConsumer;

public class DeviceSelectButton extends AbstractButton {

    private final IntConsumer updateConsumer;
    private int selected;

    public DeviceSelectButton(int x, int y, int width, int height, int selected, IntConsumer updateConsumer) {
        super(x, y, width, height, getText(selected));
        this.updateConsumer = updateConsumer;
        this.selected = selected;
    }

    @Override
    public void onPress() {
        IntList available = Capturing.getDevices();
        if (available.isEmpty()) {
            Capturing.broadcastError(Component.translatable("webcam.error.no_available"));
            selected = -1;
        } else {
            selected = available.getInt((available.indexOf(selected) + 1) % available.size());
        }
        updateConsumer.accept(selected);
        setMessage(getText(selected));
    }

    private static Component getText(int selected) {
        return Component.translatable(
                "webcam.screen.webcam.selected_device",
                selected == -1
                        ? Component.translatable("webcam.screen.webcam.selected_device.none")
                        : selected
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

}
