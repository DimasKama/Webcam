package ru.dimaskama.webcam.message;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class Channel<T extends Message> {

    public static final Channel<SecretRequestMessage> SECRET_REQUEST = new Channel<>("webcam:secret_request", SecretRequestMessage::new);
    public static final Channel<SecretMessage> SECRET = new Channel<>("webcam:secret", SecretMessage::new);
    private final String id;
    private final Function<ByteBuffer, T> decoder;

    public Channel(String id, Function<ByteBuffer, T> decoder) {
        this.id = id;
        this.decoder = decoder;
    }

    public String getId() {
        return id;
    }

    public T decode(ByteBuffer buffer) {
        return decoder.apply(buffer);
    }

}
