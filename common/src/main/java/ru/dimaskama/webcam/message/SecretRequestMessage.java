package ru.dimaskama.webcam.message;

import java.nio.ByteBuffer;

public record SecretRequestMessage(
        String version
) implements Message {

    public SecretRequestMessage(ByteBuffer buffer) {
        this(BufferUtils.readUtf8(buffer));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        BufferUtils.writeUtf8(buffer, version);
    }

    @Override
    public Channel<SecretRequestMessage> getChannel() {
        return Channel.SECRET_REQUEST;
    }

}
