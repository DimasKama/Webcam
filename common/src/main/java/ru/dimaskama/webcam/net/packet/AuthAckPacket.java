package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

public final class AuthAckPacket implements Packet {

    public static final AuthAckPacket INSTANCE = new AuthAckPacket();

    private AuthAckPacket() {}

    @Override
    public void writeBytes(ByteBuf buf) {

    }

    @Override
    public PacketType<?> getType() {
        return PacketType.AUTH_ACK;
    }

    @Override
    public int getEstimatedSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "AuthAckPacket";
    }

}
