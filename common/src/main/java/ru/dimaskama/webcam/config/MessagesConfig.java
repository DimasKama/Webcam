package ru.dimaskama.webcam.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static ru.dimaskama.webcam.config.JsonConfig.defaultedField;

public record MessagesConfig(
        String incompatibleModVersion
) {

    private static final String DEFAULT_INCOMPATIBLE_MOD_VERSION = "Incompatible Webcam version. Your - %s, server's - %s";
    public static final Codec<MessagesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            defaultedField(Codec.STRING, "incompatible_mod_version", () -> DEFAULT_INCOMPATIBLE_MOD_VERSION).forGetter(MessagesConfig::incompatibleModVersion)
    ).apply(instance, MessagesConfig::new));

    public MessagesConfig() {
        this(DEFAULT_INCOMPATIBLE_MOD_VERSION);
    }

}
