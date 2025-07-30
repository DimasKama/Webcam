package ru.dimaskama.webcam.fabric.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record HudSettings(
        float iconX,
        float iconY,
        float iconScale
) {

    public static final float DEFAULT_ICON_X = 32.0F;
    public static final float DEFAULT_ICON_Y = -16.0F;
    public static final float DEFAULT_ICON_SCALE = 1.0F;
    public static final Codec<HudSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.FLOAT, "icon_x", () -> DEFAULT_ICON_X).forGetter(HudSettings::iconX),
            defaultedField(Codec.FLOAT, "icon_y", () -> DEFAULT_ICON_Y).forGetter(HudSettings::iconY),
            defaultedField(Codec.FLOAT, "icon_scale", () -> DEFAULT_ICON_SCALE).forGetter(HudSettings::iconScale)
    ).apply(instance, HudSettings::new));

    public HudSettings() {
        this(DEFAULT_ICON_X, DEFAULT_ICON_Y, DEFAULT_ICON_SCALE);
    }

    public HudSettings withIconX(float iconX) {
        return new HudSettings(iconX, iconY, iconScale);
    }

    public HudSettings withIconY(float iconY) {
        return new HudSettings(iconX, iconY, iconScale);
    }

    public HudSettings withIconScale(float iconScale) {
        return new HudSettings(iconX, iconY, iconScale);
    }

}
