package ru.dimaskama.webcam.net.packet;

import java.nio.ByteBuffer;

public class CloseSourceC2SPacket implements Packet {

    public static final CloseSourceC2SPacket INSTANCE = new CloseSourceC2SPacket();

    private CloseSourceC2SPacket() {}

    @Override
    public void writeBytes(ByteBuffer buffer) {

    }

    @Override
    public PacketType<CloseSourceC2SPacket> getType() {
        return PacketType.CLOSE_SOURCE_C2S;
    }

    @Override
    public String toString() {
        return "CloseSourceC2SPacket";
    }

}
