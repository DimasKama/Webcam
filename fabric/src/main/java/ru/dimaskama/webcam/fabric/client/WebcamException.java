package ru.dimaskama.webcam.fabric.client;

import net.minecraft.network.chat.Component;

public class WebcamException extends RuntimeException {

    private final Component text;

    public WebcamException(Component text) {
        super(text.getString());
        this.text = text;
    }

    public WebcamException(Component text, Throwable cause) {
        super(text.getString(), cause);
        this.text = text;
    }

    public Component getText() {
        return text;
    }

}
