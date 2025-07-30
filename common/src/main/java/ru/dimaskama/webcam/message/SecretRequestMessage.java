package ru.dimaskama.webcam.message;

import io.netty.buffer.ByteBuf;

public record SecretRequestMessage(
        String version
) implements Message {

    public SecretRequestMessage(ByteBuf buf) {
        this(BufUtils.readUtf8(buf));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        BufUtils.writeUtf8(buf, version);
    }

    @Override
    public Channel<SecretRequestMessage> getChannel() {
        return Channel.SECRET_REQUEST;
    }

}
