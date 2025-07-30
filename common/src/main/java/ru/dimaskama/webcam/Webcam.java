package ru.dimaskama.webcam;

import ru.dimaskama.webcam.config.CommonConfig;
import ru.dimaskama.webcam.config.JsonConfig;
import ru.dimaskama.webcam.config.ServerConfig;
import ru.dimaskama.webcam.logger.AbstractLogger;
import ru.dimaskama.webcam.logger.StdoutLogger;
import ru.dimaskama.webcam.message.ServerMessaging;

import java.nio.file.Path;

public final class Webcam {

    public static final String MOD_ID = "webcam";
    public static final String WEBCAMCONFIG_COMMAND_PERMISSION = "webcam.command.config";
    public static final String WEBCAM_BROADCAST_PERMISSION = "webcam.broadcast";
    public static final String WEBCAM_VIEW_PERMISSION = "webcam.view";
    private static String version;
    private static int protocolVersion;
    private static AbstractLogger logger = new StdoutLogger();
    private static WebcamService service;
    private static JsonConfig<ServerConfig> serverConfig = new JsonConfig<>(
            "config/webcam/server.json",
            ServerConfig.CODEC,
            ServerConfig::new
    );
    private static boolean debugMode;

    public static void initLogger(AbstractLogger logger) {
        Webcam.logger = logger;
    }

    public static void init(String version, Path configDir, WebcamService service) {
        Webcam.version = version;
        protocolVersion = getProtocolVersion(version);
        Webcam.service = service;
        JsonConfig<CommonConfig> commonConfig = new JsonConfig<>(
                configDir.resolve("common.json").toString(),
                CommonConfig.CODEC,
                CommonConfig::new
        );
        serverConfig = new JsonConfig<>(
                configDir.resolve("server.json").toString(),
                ServerConfig.CODEC,
                ServerConfig::new
        );
        commonConfig.loadOrCreate();
        debugMode = commonConfig.getData().debugMode();
        ServerMessaging.init();
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

    public static JsonConfig<ServerConfig> getServerConfig() {
        return serverConfig;
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
        return debugMode;
    }

}
