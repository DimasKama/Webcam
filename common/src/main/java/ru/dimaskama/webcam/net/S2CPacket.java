package ru.dimaskama.webcam.net;

import ru.dimaskama.webcam.net.packet.Packet;

import java.nio.ByteBuffer;
import java.util.UUID;

// Utility class
public final class S2CPacket {

    public static byte MAGIC = (byte) 0b11101110;

    public static byte[] create(UUID playerUuid, byte[] encrypted) throws Exception {
        byte[] result = new byte[17 + encrypted.length];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer
                .put(MAGIC)
                .putLong(playerUuid.getMostSignificantBits())
                .putLong(playerUuid.getLeastSignificantBits())
                .put(encrypted);
        return result;
    }

    public static Packet decrypt(byte[] data, UUID secret) throws Exception {
        if (data[0] != MAGIC) {
            throw new IllegalArgumentException("Invalid packet");
        }
        // Skip UUID
        return Packet.decrypt(data, 17, data.length - 17, secret);
    }

}
