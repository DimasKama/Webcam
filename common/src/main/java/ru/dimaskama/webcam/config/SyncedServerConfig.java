package ru.dimaskama.webcam.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record SyncedServerConfig(
        int imageDimension,
        int mtu,
        int bitrate
) {

    public static final int MIN_IMAGE_DIMENSION = 16;
    public static final int MAX_IMAGE_DIMENSION = 1440;
    public static final int MIN_MTU = 128;
    public static final int MAX_MTU = 2048;
    public static final int MIN_BITRATE = 10;
    public static final int MAX_BITRATE = 10_000;
    public static final Codec<SyncedServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.intRange(MIN_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION), "image_dimension", () -> 360).forGetter(SyncedServerConfig::imageDimension),
            defaultedField(Codec.intRange(MIN_MTU, MAX_MTU), "mtu", () -> 1100).forGetter(SyncedServerConfig::mtu),
            defaultedField(Codec.intRange(MIN_BITRATE, MAX_BITRATE), "bitrate", () -> 500).forGetter(SyncedServerConfig::bitrate)
    ).apply(instance, SyncedServerConfig::new));

    public SyncedServerConfig() {
        this(360, 1100, 500);
    }

    public SyncedServerConfig(ByteBuf buf) {
        this(buf.readShort(), buf.readShort(), buf.readInt());
    }

    public void writeBytes(ByteBuf buf) {
        buf.writeShort(imageDimension)
                .writeShort(mtu)
                .writeInt(bitrate);
    }

    public SyncedServerConfig withImageDimension(int imageDimension) {
        return new SyncedServerConfig(imageDimension, mtu, bitrate);
    }

    public SyncedServerConfig withMtu(int mtu) {
        return new SyncedServerConfig(imageDimension, mtu, bitrate);
    }

    public SyncedServerConfig withBitrate(int bitrate) {
        return new SyncedServerConfig(imageDimension, mtu, bitrate);
    }

}
