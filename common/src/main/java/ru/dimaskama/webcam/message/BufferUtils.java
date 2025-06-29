package ru.dimaskama.webcam.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BufferUtils {

    public static int readVarInt(ByteBuffer buffer) {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = buffer.get();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b & 128) == 128);

        return i;
    }

    public static void writeVarInt(ByteBuffer buffer, int i) {
        while((i & -128) != 0) {
            buffer.put((byte) (i & 127 | 128));
            i >>>= 7;
        }

        buffer.put((byte) i);
    }

    public static UUID readUuid(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static void writeUuid(ByteBuffer buf, UUID uuid) {
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
    }

    public static String readUtf8(ByteBuffer buffer) {
        int size = readVarInt(buffer);
        byte[] strBytes = new byte[size];
        buffer.get(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    public static void writeUtf8(ByteBuffer buffer, String string) {
        byte[] strData = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buffer, strData.length);
        buffer.put(strData);
    }

}
