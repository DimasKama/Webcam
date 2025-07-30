package ru.dimaskama.webcam.command;

import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.config.ServerConfig;
import ru.dimaskama.webcam.config.SyncedServerConfig;
import ru.dimaskama.webcam.config.VideoDisplayShape;
import ru.dimaskama.webcam.net.packet.ServerConfigPacket;
import ru.dimaskama.webcam.server.WebcamServer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class WebcamconfigCommand {

    public static final String COMMAND_NAME = "webcamconfig";
    private static final Map<String, ConfigField<?>> FIELDS = new HashMap<>();

    static {
        putIntField("port", 0, 65535, ServerConfig::port, ServerConfig::withPort);
        putStringField("bind_address", ServerConfig::bindAddress, ServerConfig::withBindAddress);
        putStringField("host", ServerConfig::host, ServerConfig::withHost);
        putIntField("keep_alive_period", 500, 100_000, ServerConfig::keepAlivePeriod, ServerConfig::withKeepAlivePeriod);
        putIntField("permission_check_period", 1, 100_000, ServerConfig::permissionCheckPeriod, ServerConfig::withPermissionCheckPeriod);
        putDoubleField("max_display_distance", ServerConfig.MIN_MAX_DISPLAY_DISTANCE, ServerConfig.MAX_MAX_DISPLAY_DISTANCE, ServerConfig::maxDisplayDistance, ServerConfig::withMaxDisplayDistance);
        putBooleanField("display_on_face", ServerConfig::displayOnFace, ServerConfig::withDisplayOnFace);
        putField(new ConfigField<>("display_shape", ServerConfig::displayShape, ServerConfig::withDisplayShape, VideoDisplayShape::byKey));
        putFloatField("display_offset_y", ServerConfig.MIN_DISPLAY_OFFSET_Y, ServerConfig.MAX_DISPLAY_OFFSET_Y, ServerConfig::displayOffsetY, ServerConfig::withDisplayOffsetY);
        putFloatField("display_size", ServerConfig.MIN_DISPLAY_SIZE, ServerConfig.MAX_DISPLAY_SIZE, ServerConfig::displaySize, ServerConfig::withDisplaySize);
        putBooleanField("hide_nicknames", ServerConfig::hideNicknames, ServerConfig::withHideNicknames);
        putBooleanField("display_self_webcam", ServerConfig::displaySelfWebcam, ServerConfig::withDisplaySelfWebcam);
        putIntField("image_dimension", SyncedServerConfig.MIN_IMAGE_DIMENSION, SyncedServerConfig.MAX_IMAGE_DIMENSION, c -> c.synced().imageDimension(), (c, i) -> c.withSynced(c.synced().withImageDimension(i)));
        putIntField("mtu", SyncedServerConfig.MIN_MTU, SyncedServerConfig.MAX_MTU, c -> c.synced().mtu(), (c, i) -> c.withSynced(c.synced().withMtu(i)));
        putIntField("bitrate", SyncedServerConfig.MIN_BITRATE, SyncedServerConfig.MAX_BITRATE, c -> c.synced().bitrate(), (c, i) -> c.withSynced(c.synced().withBitrate(i)));
    }

    private static void putField(ConfigField<?> field) {
        FIELDS.put(field.name, field);
    }

    private static void putIntField(
            String name,
            int min,
            int max,
            Function<ServerConfig, Integer> getter,
            BiFunction<ServerConfig, Integer, ServerConfig> withFunction
    ) {
        putField(new ConfigField<>(
                name,
                getter,
                withFunction,
                string -> {
                    int intValue;
                    try {
                        intValue = Integer.parseInt(string);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid integer: " + string);
                    }
                    if (intValue < min || intValue > max) {
                        throw new IllegalArgumentException("Value is not in range [" + min + ";" + max + "]");
                    }
                    return intValue;
                }
        ));
    }

    private static void putFloatField(
            String name,
            float min,
            float max,
            Function<ServerConfig, Float> getter,
            BiFunction<ServerConfig, Float, ServerConfig> withFunction
    ) {
        putField(new ConfigField<>(
                name,
                getter,
                withFunction,
                string -> {
                    float floatValue;
                    try {
                        floatValue = Float.parseFloat(string);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid float: " + string);
                    }
                    if (floatValue < min || floatValue > max) {
                        throw new IllegalArgumentException("Value is not in range [" + min + ";" + max + "]");
                    }
                    return floatValue;
                }
        ));
    }

    private static void putDoubleField(
            String name,
            double min,
            double max,
            Function<ServerConfig, Double> getter,
            BiFunction<ServerConfig, Double, ServerConfig> withFunction
    ) {
        putField(new ConfigField<>(
                name,
                getter,
                withFunction,
                string -> {
                    double doubleValue;
                    try {
                        doubleValue = Float.parseFloat(string);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid float: " + string);
                    }
                    if (doubleValue < min || doubleValue > max) {
                        throw new IllegalArgumentException("Value is not in range [" + min + ";" + max + "]");
                    }
                    return doubleValue;
                }
        ));
    }

    private static void putStringField(
            String name,
            Function<ServerConfig, String> getter,
            BiFunction<ServerConfig, String, ServerConfig> withFunction
    ) {
        putField(new ConfigField<>(
                name,
                getter,
                withFunction,
                Function.identity()
        ));
    }

    private static void putBooleanField(
            String name,
            Function<ServerConfig, Boolean> getter,
            BiFunction<ServerConfig, Boolean, ServerConfig> withFunction
    ) {
        putField(new ConfigField<>(
                name,
                getter,
                withFunction,
                Boolean::parseBoolean
        ));
    }

    public static Stream<String> suggestFields(String start) {
        String lowerCase = start.toLowerCase(Locale.ROOT);
        return FIELDS.keySet().stream().filter(s -> s.startsWith(lowerCase));
    }

    public static String getField(String field) throws IllegalArgumentException {
        ServerConfig config = Webcam.getServerConfig().getData();
        ConfigField<?> configField = FIELDS.get(field);
        if (configField != null) {
            return configField.name + ": " + configField.getter.apply(config);
        }
        unknownField(field);
        return "";
    }

    public static String setField(String field, String newValue) throws IllegalArgumentException {
        ServerConfig config = Webcam.getServerConfig().getData();
        ConfigField<?> configField = FIELDS.get(field);
        if (configField != null) {
            ServerConfig newConfig = setNewValue(config, configField, newValue);
            updateConfig(config, newConfig);
            return configField.name + " set to " + configField.getter.apply(newConfig);
        }
        unknownField(field);
        return "";
    }

    private static <T> ServerConfig setNewValue(ServerConfig config, ConfigField<T> configField, String newValue) {
        return configField.withFunction.apply(config, configField.parser.apply(newValue));
    }

    private static void updateConfig(ServerConfig oldConfig, ServerConfig config) {
        if (!oldConfig.equals(config)) {
            Webcam.getServerConfig().setData(config);
            Webcam.getServerConfig().save();
            if (!oldConfig.synced().equals(config.synced())) {
                WebcamServer server = WebcamServer.getInstance();
                if (server != null) {
                    server.broadcast(new ServerConfigPacket(config.synced()));
                }
            }
        }
    }

    private static void unknownField(String field) throws IllegalArgumentException {
        throw new IllegalArgumentException("Unknown field: " + field);
    }

    private record ConfigField<T>(
            String name,
            Function<ServerConfig, T> getter,
            BiFunction<ServerConfig, T, ServerConfig> withFunction,
            Function<String, T> parser
    ) {

    }

}
