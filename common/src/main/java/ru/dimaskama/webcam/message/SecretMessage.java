package ru.dimaskama.webcam.message;

import java.nio.ByteBuffer;
import java.util.UUID;

public record SecretMessage(
        UUID secret,
        int serverPort,
        int keepAlivePeriod,
        String host
) implements Message {

    public SecretMessage(ByteBuffer buffer) {
        this(BufferUtils.readUuid(buffer), buffer.getShort() & 0xFFFF, buffer.getInt(), BufferUtils.readUtf8(buffer));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        BufferUtils.writeUuid(buffer, secret);
        buffer.putShort((short) serverPort);
        buffer.putInt(keepAlivePeriod);
        BufferUtils.writeUtf8(buffer, host);
    }

    @Override
    public Channel<SecretMessage> getChannel() {
        return Channel.SECRET;
    }

}
