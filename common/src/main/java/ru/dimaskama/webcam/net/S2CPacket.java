package ru.dimaskama.webcam.net;

import ru.dimaskama.webcam.net.packet.Packet;

import java.util.UUID;

// Utility class
public final class S2CPacket {

    public static byte MAGIC = (byte) 0b11101110;

    public static byte[] create(byte[] encrypted) throws Exception {
        byte[] result = new byte[1 + encrypted.length];
        result[0] = MAGIC;
        System.arraycopy(encrypted, 0, result, 1, encrypted.length);
        return result;
    }

    public static Packet decrypt(byte[] data, UUID secret) throws Exception {
        if (data[0] != MAGIC) {
            throw new IllegalArgumentException("Invalid packet");
        }
        return Packet.decrypt(data, 1, data.length - 1, secret);
    }

}
