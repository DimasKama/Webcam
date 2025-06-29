package ru.dimaskama.webcam.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.nio.ByteBuffer;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record SyncedServerConfig(
        int imageDimension,
        int mtu
) {

    public static final int MIN_IMAGE_DIMENSION = 16;
    public static final int MAX_IMAGE_DIMENSION = 1440;
    public static final int MIN_MTU = 128;
    public static final int MAX_MTU = 2048;
    public static final Codec<SyncedServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.intRange(MIN_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION), "image_dimension", () -> 360).forGetter(SyncedServerConfig::imageDimension),
            defaultedField(Codec.intRange(MIN_MTU, MAX_MTU), "mtu", () -> 1300).forGetter(SyncedServerConfig::mtu)
    ).apply(instance, SyncedServerConfig::new));

    public SyncedServerConfig() {
        this(360, 1300);
    }

    public SyncedServerConfig(ByteBuffer buffer) {
        this(buffer.getShort(), buffer.getShort());
    }

    public void writeBytes(ByteBuffer buffer) {
        buffer.putShort((short) imageDimension)
                .putShort((short) mtu);
    }

    public SyncedServerConfig withImageDimension(int imageDimension) {
        return new SyncedServerConfig(imageDimension, mtu);
    }

    public SyncedServerConfig withMtu(int mtu) {
        return new SyncedServerConfig(imageDimension, mtu);
    }

}
