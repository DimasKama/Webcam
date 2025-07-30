package ru.dimaskama.webcam.net;

import io.netty.buffer.ByteBuf;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public class Encryption {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static SecretKey uuidToKey(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return new SecretKeySpec(buffer.array(), "AES");
    }

    public static byte[] encrypt(byte[] data, int offset, int length, SecretKey secretKey) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        byte[] cipherText = cipher.doFinal(data, offset, length);
        byte[] payload = new byte[IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, payload, 0, IV_LENGTH);
        System.arraycopy(cipherText, 0, payload, IV_LENGTH, cipherText.length);
        return payload;
    }

    public static byte[] decrypt(ByteBuf buf, SecretKey secretKey) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        buf.readBytes(iv);
        byte[] cipherText = new byte[buf.readableBytes()];
        buf.readBytes(cipherText);
        return decrypt(iv, cipherText, secretKey);
    }

    private static byte[] decrypt(byte[] iv, byte[] cipherText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        return cipher.doFinal(cipherText);
    }

}