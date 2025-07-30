package ru.dimaskama.webcam.message;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public record SecretMessage(
        UUID secret,
        int serverPort,
        int keepAlivePeriod,
        String host
) implements Message {

    public SecretMessage(ByteBuf buf) {
        this(new UUID(buf.readLong(), buf.readLong()), buf.readShort() & 0xFFFF, buf.readInt(), BufUtils.readUtf8(buf));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeLong(secret.getMostSignificantBits())
                .writeLong(secret.getLeastSignificantBits());
        buf.writeShort(serverPort);
        buf.writeInt(keepAlivePeriod);
        BufUtils.writeUtf8(buf, host);
    }

    @Override
    public Channel<SecretMessage> getChannel() {
        return Channel.SECRET;
    }

}
