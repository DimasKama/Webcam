package ru.dimaskama.webcam.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record CommonConfig(
        boolean debugMode
) {

    public static final Codec<CommonConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.BOOL, "debug_mode", () -> false).forGetter(CommonConfig::debugMode)
    ).apply(instance, CommonConfig::new));

    public CommonConfig() {
        this(false);
    }

}
