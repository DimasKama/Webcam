package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public record AddBlockedSourceC2SPacket(
        UUID uuid
) implements Packet {

    public AddBlockedSourceC2SPacket(ByteBuf buf) {
        this(new UUID(buf.readLong(), buf.readLong()));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public PacketType<AddBlockedSourceC2SPacket> getType() {
        return PacketType.ADD_BLOCKED_SOURCE_C2S;
    }

    @Override
    public int getEstimatedSize() {
        return 16;
    }

}
