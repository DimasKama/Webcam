package ru.dimaskama.webcam.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum VideoDisplayShape {

    SQUARE("square", (byte) 0),
    ROUND("round", (byte) 1);

    public static final Codec<VideoDisplayShape> CODEC = Codec.STRING.comapFlatMap(str -> {
        for (VideoDisplayShape shape : values()) {
            if (str.equalsIgnoreCase(shape.key)) {
                return DataResult.success(shape);
            }
        }
        return DataResult.error(() -> "Unknown shape: " + str);
    }, shape -> shape.key);
    public final String key;
    public final byte code;

    VideoDisplayShape(String key, byte code) {
        this.key = key;
        this.code = code;
    }

    public static VideoDisplayShape byCode(byte code) {
        for (VideoDisplayShape type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + Integer.toBinaryString(code));
    }

    public static VideoDisplayShape byKey(String key) {
        for (VideoDisplayShape type : values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown shape: " + key);
    }

}
