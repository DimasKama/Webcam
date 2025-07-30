package ru.dimaskama.webcam.message;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class BufUtils {

    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = buf.readByte();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b & 128) == 128);

        return i;
    }

    public static void writeVarInt(ByteBuf buf, int i) {
        while((i & -128) != 0) {
            buf.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        buf.writeByte(i);
    }

    public static String readUtf8(ByteBuf buf) {
        int size = readVarInt(buf);
        byte[] strBytes = new byte[size];
        buf.readBytes(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    public static void writeUtf8(ByteBuf buf, String string) {
        byte[] strData = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, strData.length);
        buf.writeBytes(strData);
    }

}
