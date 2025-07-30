package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

public final class CloseSourceC2SPacket implements Packet {

    public static final CloseSourceC2SPacket INSTANCE = new CloseSourceC2SPacket();

    private CloseSourceC2SPacket() {}

    @Override
    public void writeBytes(ByteBuf buf) {

    }

    @Override
    public PacketType<CloseSourceC2SPacket> getType() {
        return PacketType.CLOSE_SOURCE_C2S;
    }

    @Override
    public int getEstimatedSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "CloseSourceC2SPacket";
    }

}
