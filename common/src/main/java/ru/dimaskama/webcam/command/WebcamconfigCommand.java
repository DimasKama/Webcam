package ru.dimaskama.webcam.command;

import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.config.ServerConfig;
import ru.dimaskama.webcam.config.SyncedServerConfig;
import ru.dimaskama.webcam.config.VideoDisplayShape;
import ru.dimaskama.webcam.server.WebcamServer;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class WebcamconfigCommand {

    public static final String COMMAND_NAME = "webcamconfig";
    private static final List<String> FIELDS = List.of("max_display_distance", "display_on_face", "display_shape", "display_offset_y", "display_size", "hide_nicknames", "display_self_webcam", "image_dimension", "mtu");

    public static Stream<String> suggestFields(String start) {
        String lowerCase = start.toLowerCase(Locale.ROOT);
        return FIELDS.stream().filter(s -> s.startsWith(lowerCase));
    }

    public static String getField(String field) throws IllegalArgumentException {
        ServerConfig config = Webcam.SERVER_CONFIG.getData();
        if (field.equalsIgnoreCase("max_display_distance")) {
            return "max_display_distance: " + config.maxDisplayDistance();
        }
        if (field.equalsIgnoreCase("display_on_face")) {
            return "display_on_face: " + config.displayOnFace();
        }
        if (field.equalsIgnoreCase("display_shape")) {
            return "display_shape: " + config.displayShape().key;
        }
        if (field.equalsIgnoreCase("display_offset_y")) {
            return "display_offset_y: " + config.displayOffsetY();
        }
        if (field.equalsIgnoreCase("display_size")) {
            return "display_size: " + config.displaySize();
        }
        if (field.equalsIgnoreCase("hide_nicknames")) {
            return "hide_nicknames: " + config.hideNicknames();
        }
        if (field.equalsIgnoreCase("display_self_webcam")) {
            return "display_self_webcam: " + config.displaySelfWebcam();
        }
        if (field.equalsIgnoreCase("image_dimension")) {
            return "image_dimension: " + config.synced().imageDimension();
        }
        if (field.equalsIgnoreCase("mtu")) {
            return "mtu: " + config.synced().mtu();
        }
        unknownField(field);
        return "";
    }

    public static String setField(String field, String newValue) throws IllegalArgumentException {
        ServerConfig config = Webcam.SERVER_CONFIG.getData();
        if (field.equalsIgnoreCase("max_display_distance")) {
            double doubleValue = getDoubleValue(newValue, ServerConfig.MIN_MAX_DISPLAY_DISTANCE, ServerConfig.MAX_MAX_DISPLAY_DISTANCE);
            updateConfig(config, config.withMaxDisplayDistance(doubleValue));
            return "max_display_distance set to " + doubleValue;
        }
        if (field.equalsIgnoreCase("display_on_face")) {
            boolean booleanValue = Boolean.parseBoolean(newValue);
            updateConfig(config, config.withDisplayOnFace(booleanValue));
            return "display_on_face set to " + booleanValue;
        }
        if (field.equalsIgnoreCase("display_shape")) {
            VideoDisplayShape shape = VideoDisplayShape.byKey(newValue);
            updateConfig(config, config.withDisplayShape(shape));
            return "display_shape set to " + shape.key;
        }
        if (field.equalsIgnoreCase("display_offset_y")) {
            float floatValue = getFloatValue(newValue, ServerConfig.MIN_DISPLAY_OFFSET_Y, ServerConfig.MAX_DISPLAY_OFFSET_Y);
            updateConfig(config, config.withDisplayOffsetY(floatValue));
            return "display_offset_y set to " + floatValue;
        }
        if (field.equalsIgnoreCase("display_size")) {
            float floatValue = getFloatValue(newValue, ServerConfig.MIN_DISPLAY_SIZE, ServerConfig.MAX_DISPLAY_SIZE);
            updateConfig(config, config.withDisplaySize(floatValue));
            return "display_size set to " + floatValue;
        }
        if (field.equalsIgnoreCase("hide_nicknames")) {
            boolean booleanValue = Boolean.parseBoolean(newValue);
            updateConfig(config, config.withHideNicknames(booleanValue));
            return "hide_nicknames set to " + booleanValue;
        }
        if (field.equalsIgnoreCase("display_self_webcam")) {
            boolean booleanValue = Boolean.parseBoolean(newValue);
            updateConfig(config, config.withDisplaySelfWebcam(booleanValue));
            return "display_self_webcam set to " + booleanValue;
        }
        if (field.equalsIgnoreCase("image_dimension")) {
            int intValue = getIntValue(newValue, SyncedServerConfig.MIN_IMAGE_DIMENSION, SyncedServerConfig.MAX_IMAGE_DIMENSION);
            updateConfig(config, config.withSynced(config.synced().withImageDimension(intValue)));
            return "image_dimension set to " + intValue;
        }
        if (field.equalsIgnoreCase("mtu")) {
            int intValue = getIntValue(newValue, SyncedServerConfig.MIN_MTU, SyncedServerConfig.MAX_MTU);
            updateConfig(config, config.withSynced(config.synced().withMtu(intValue)));
            return "mtu set to " + intValue;
        }
        unknownField(field);
        return "";
    }

    private static double getDoubleValue(String newValue, double min, double max) throws IllegalArgumentException {
        double doubleValue;
        try {
            doubleValue = Float.parseFloat(newValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid float: " + newValue);
        }
        if (doubleValue < min || doubleValue > max) {
            throw new IllegalArgumentException("Value is not in range [" + min + ";" + max + "]");
        }
        return doubleValue;
    }

    private static int getIntValue(String newValue, int min, int max) throws IllegalArgumentException {
        int intValue;
        try {
            intValue = Integer.parseInt(newValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid integer: " + newValue);
        }
        if (intValue < min || intValue > max) {
            throw new IllegalArgumentException("Value is not in range [" + min + ";" + max + "]");
        }
        return intValue;
    }

    private static void updateConfig(ServerConfig oldConfig, ServerConfig config) {
        if (!oldConfig.equals(config)) {
            Webcam.SERVER_CONFIG.setData(config);
            Webcam.SERVER_CONFIG.save();
            if (!oldConfig.synced().equals(config.synced())) {
                WebcamServer server = WebcamServer.getInstance();
                if (server != null) {
                    server.broadcastConfig(config.synced());
                }
            }
        }
    }

    private static float getFloatValue(String newValue, float min, float max) throws IllegalArgumentException {
        float floatValue;
        try {
            floatValue = Float.parseFloat(newValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid float: " + newValue);
        }
        if (floatValue < min || floatValue > max) {
            throw new IllegalArgumentException("Value is not in range [" + min + ";" + max + "]");
        }
        return floatValue;
    }

    private static void unknownField(String field) throws IllegalArgumentException {
        throw new IllegalArgumentException("Unknown field: " + field);
    }

}
