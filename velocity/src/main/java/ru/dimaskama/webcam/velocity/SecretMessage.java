package ru.dimaskama.webcam.velocity;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record SecretMessage(
        UUID secret,
        int serverPort,
        int keepAlivePeriod,
        String host
) {

    public static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("webcam", "secret");

    public SecretMessage(ByteBuffer buffer) {
        this(new UUID(buffer.getLong(), buffer.getLong()), buffer.getShort() & 0xFFFF, buffer.getInt(), readUtf8(buffer));
    }

    public void writeBytes(ByteBuffer buffer) {
        buffer.putLong(secret.getMostSignificantBits())
                .putLong(secret.getLeastSignificantBits())
                .putShort((short) serverPort)
                .putInt(keepAlivePeriod);
        writeUtf8(buffer, host);
    }

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
