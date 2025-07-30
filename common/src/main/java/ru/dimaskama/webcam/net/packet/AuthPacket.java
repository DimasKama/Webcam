package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public record AuthPacket(
        UUID playerUuid,
        UUID secret
) implements Packet {

    public AuthPacket(ByteBuf buf) {
        this(new UUID(buf.readLong(), buf.readLong()), new UUID(buf.readLong(), buf.readLong()));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeLong(playerUuid.getMostSignificantBits())
                .writeLong(playerUuid.getLeastSignificantBits())
                .writeLong(secret.getMostSignificantBits())
                .writeLong(secret.getLeastSignificantBits());
    }

    @Override
    public int getEstimatedSize() {
        return 32;
    }

    @Override
    public PacketType<AuthPacket> getType() {
        return PacketType.AUTH;
    }

}
