package ru.dimaskama.webcam.net.packet;

import java.nio.ByteBuffer;

public class KeepAlivePacket implements Packet {

    public static final KeepAlivePacket INSTANCE = new KeepAlivePacket();

    private KeepAlivePacket() {}

    @Override
    public void writeBytes(ByteBuffer buffer) {

    }

    @Override
    public PacketType<KeepAlivePacket> getType() {
        return PacketType.KEEP_ALIVE;
    }

    @Override
    public String toString() {
        return "KeepAlivePacket";
    }

}
