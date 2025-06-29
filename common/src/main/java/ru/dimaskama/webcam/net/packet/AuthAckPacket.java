package ru.dimaskama.webcam.net.packet;

import java.nio.ByteBuffer;

public class AuthAckPacket implements Packet {

    public static final AuthAckPacket INSTANCE = new AuthAckPacket();

    private AuthAckPacket() {}

    @Override
    public void writeBytes(ByteBuffer buffer) {

    }

    @Override
    public PacketType<?> getType() {
        return PacketType.AUTH_ACK;
    }

    @Override
    public String toString() {
        return "AuthAckPacket";
    }

}
