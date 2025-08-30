package ru.dimaskama.webcam.client.config;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum Resolution implements StringRepresentable {

    R_640X480("640x480", AspectRatio.R_4X3, 640, 480),
    R_1280X720("1280x720", AspectRatio.R_16X9, 1280, 720),
    R_1920X1080("1920x1080", AspectRatio.R_16X9, 1920, 1080),
    R_2560X1440("2560x1440", AspectRatio.R_16X9, 2560, 1440),
    R_3840X2160("3840x2160", AspectRatio.R_16X9, 3840, 2160);

    public static final Codec<Resolution> CODEC = StringRepresentable.fromValues(Resolution::values);
    public final String key;
    public final AspectRatio aspectRatio;
    public final int width;
    public final int height;

    Resolution(String key, AspectRatio aspectRatio, int width, int height) {
        this.key = key;
        this.aspectRatio = aspectRatio;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getSerializedName() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }

    public enum AspectRatio {

        R_4X3("4x3"),
        R_16X9("16x9");

        public final String key;

        AspectRatio(String key) {
            this.key = key;
        }

    }

}
