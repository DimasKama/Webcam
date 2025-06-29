package ru.dimaskama.webcam.net.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

public record CloseSourceS2CPacket(
        UUID sourceUuid
) implements Packet {

    public CloseSourceS2CPacket(ByteBuffer buffer) {
        this(new UUID(buffer.getLong(), buffer.getLong()));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        buffer.putLong(sourceUuid.getMostSignificantBits()).putLong(sourceUuid.getLeastSignificantBits());
    }

    @Override
    public PacketType<CloseSourceS2CPacket> getType() {
        return PacketType.CLOSE_SOURCE_S2C;
    }

}
