package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

public final class KeepAlivePacket implements Packet {

    public static final KeepAlivePacket INSTANCE = new KeepAlivePacket();

    private KeepAlivePacket() {}

    @Override
    public void writeBytes(ByteBuf buf) {

    }

    @Override
    public PacketType<KeepAlivePacket> getType() {
        return PacketType.KEEP_ALIVE;
    }

    @Override
    public int getEstimatedSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "KeepAlivePacket";
    }

}
