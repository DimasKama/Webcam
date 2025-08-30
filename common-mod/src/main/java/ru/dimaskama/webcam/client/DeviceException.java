package ru.dimaskama.webcam.client;

import net.minecraft.network.chat.Component;

public class DeviceException extends RuntimeException {

    private final Component text;

    public DeviceException(Component text) {
        super(text.getString());
        this.text = text;
    }

    public DeviceException(Component text, Throwable cause) {
        super(text.getString(), cause);
        this.text = text;
    }

    public Component getText() {
        return text;
    }

    public static DeviceException wrap(int deviceNumber, Throwable cause) {
        if (cause instanceof DeviceException e) {
            return e;
        }
        return new DeviceException(Component.translatable("webcam.error.device_unknown_error", deviceNumber), cause);
    }

}
