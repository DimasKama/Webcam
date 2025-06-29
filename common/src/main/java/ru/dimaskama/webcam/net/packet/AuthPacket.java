package ru.dimaskama.webcam.net.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

public record AuthPacket(
        UUID playerUuid,
        UUID secret
) implements Packet {

    public AuthPacket(ByteBuffer buffer) {
        this(new UUID(buffer.getLong(), buffer.getLong()), new UUID(buffer.getLong(), buffer.getLong()));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        buffer
                .putLong(playerUuid.getMostSignificantBits())
                .putLong(playerUuid.getLeastSignificantBits())
                .putLong(secret.getMostSignificantBits())
                .putLong(secret.getLeastSignificantBits());
    }

    @Override
    public PacketType<AuthPacket> getType() {
        return PacketType.AUTH;
    }

}
