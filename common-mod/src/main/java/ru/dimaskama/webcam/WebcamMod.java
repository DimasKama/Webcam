package ru.dimaskama.webcam;

import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class WebcamMod {

    private static WebcamModService service;

    public static void init(String modVersion, Path configDir, WebcamService service, WebcamModService modService) {
        Webcam.init(splitModVersion(modVersion), configDir, service);
        WebcamMod.service = modService;
    }

    public static WebcamModService getService() {
        return service;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Webcam.MOD_ID, path);
    }

    private static String splitModVersion(String modVersion) {
        int firstDash = modVersion.indexOf('-');
        return firstDash != -1 ? modVersion.substring(0, firstDash) : modVersion;
    }

}
