package ru.dimaskama.webcam.net;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

public class Encryption {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CIPHER = "AES/CBC/PKCS5Padding";

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        RANDOM.nextBytes(iv);
        return iv;
    }

    private static SecretKeySpec createKeySpec(UUID secret) {
        return new SecretKeySpec(getBytesFromUUID(secret), "AES");
    }

    public static byte[] encrypt(byte[] data, int start, int len, UUID secret) throws Exception {
        byte[] iv = generateIV();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, createKeySpec(secret), ivSpec);
        byte[] enc = cipher.doFinal(data, start, len);
        byte[] payload = new byte[iv.length + enc.length];
        System.arraycopy(iv, 0, payload, 0, iv.length);
        System.arraycopy(enc, 0, payload, iv.length, enc.length);
        return payload;
    }

    public static byte[] decrypt(byte[] payload, int start, int len, UUID secret) throws Exception {
        byte[] iv = new byte[16];
        System.arraycopy(payload, start, iv, 0, iv.length);
        byte[] data = new byte[len - iv.length];
        System.arraycopy(payload, start + iv.length, data, 0, data.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, createKeySpec(secret), ivSpec);
        return cipher.doFinal(data);
    }

}