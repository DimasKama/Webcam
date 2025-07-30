package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public record CloseSourceS2CPacket(
        UUID sourceUuid
) implements Packet {

    public CloseSourceS2CPacket(ByteBuf buf) {
        this(new UUID(buf.readLong(), buf.readLong()));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeLong(sourceUuid.getMostSignificantBits()).writeLong(sourceUuid.getLeastSignificantBits());
    }

    @Override
    public int getEstimatedSize() {
        return 16;
    }

    @Override
    public PacketType<CloseSourceS2CPacket> getType() {
        return PacketType.CLOSE_SOURCE_S2C;
    }

}
