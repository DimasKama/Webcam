package ru.dimaskama.webcam;

import ru.dimaskama.webcam.config.CommonConfig;
import ru.dimaskama.webcam.config.JsonConfig;
import ru.dimaskama.webcam.config.ServerConfig;
import ru.dimaskama.webcam.logger.AbstractLogger;
import ru.dimaskama.webcam.logger.StdoutLogger;
import ru.dimaskama.webcam.message.ServerMessaging;

public final class Webcam {

    public static final JsonConfig<CommonConfig> COMMON_CONFIG = new JsonConfig<>(
            "config/webcam/common.json",
            CommonConfig.CODEC,
            CommonConfig::new
    );
    public static final JsonConfig<ServerConfig> SERVER_CONFIG = new JsonConfig<>(
            "config/webcam/server.json",
            ServerConfig.CODEC,
            ServerConfig::new
    );
    public static final String MOD_ID = "webcam";
    public static final String WEBCAMCONFIG_COMMAND_PERMISSION = "webcam.command.config";
    private static String version;
    private static int protocolVersion;
    private static AbstractLogger logger = new StdoutLogger();
    private static WebcamService service;

    public static void initLogger(AbstractLogger logger) {
        Webcam.logger = logger;
    }

    public static void init(String version, WebcamService service) {
        Webcam.version = version;
        protocolVersion = getProtocolVersion(version);
        Webcam.service = service;
        ServerMessaging.init();
        COMMON_CONFIG.loadOrCreate();
    }

    public static String getVersion() {
        return version;
    }

    public static AbstractLogger getLogger() {
        return logger;
    }

    public static WebcamService getService() {
        return service;
    }

    public static boolean isClientVersionCompatible(String version) {
        return protocolVersion == getProtocolVersion(version);
    }

    public static int getProtocolVersion(String version) {
        int firstPeriod = version.indexOf('.');
        if (firstPeriod == -1) {
            firstPeriod = version.length();
        }
        try {
            return Integer.parseInt(version.substring(0, firstPeriod));
        } catch (Exception ignored) {}
        return -1;
    }

    public static boolean isDebugMode() {
        return COMMON_CONFIG.getData().debugMode();
    }

}
