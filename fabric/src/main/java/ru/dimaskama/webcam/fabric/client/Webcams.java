package ru.dimaskama.webcam.fabric.client;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.client.Minecraft;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.fabric.client.config.ClientConfig;
import ru.dimaskama.webcam.fabric.client.config.Resolution;
import ru.dimaskama.webcam.fabric.client.screen.WebcamScreen;
import ru.dimaskama.webcam.net.packet.CloseSourceC2SPacket;

// Maintained by RenderThread
public class Webcams {

    private static IntList devices = IntList.of();
    private static CapturingDevice current;

    public static void init() {
        Webcam.getLogger().info("Loading OpenCV...");
        OpenCV.loadLocally();
        if (Webcam.isDebugMode()) {
            Webcam.getLogger().info("OpenCV build information: " + Core.getBuildInformation());
        }
    }

    // OpenCV has no function to list available devices, so we have to try to start VideoCapture on each index
    public static void updateDevices() {
        Webcam.getLogger().info("Updating available webcam devices. Some errors may be printed");
        int max = WebcamFabricClient.CONFIG.getData().maxDevices();
        IntList list = new IntArrayList();
        for (int i = 0; i < max; i++) {
            VideoCapture capture = null;
            try {
                capture = new VideoCapture(i);
                if (capture.isOpened()) {
                    list.add(i);
                }
            } finally {
                if (capture != null) {
                    capture.release();
                }
            }
        }
        devices = IntLists.unmodifiable(list);
    }

    public static IntList getDevices() {
        return devices;
    }

    public static void updateCapturing() {
        ClientConfig config = WebcamFabricClient.CONFIG.getData();
        updateCapturing(config.webcamEnabled(), config.selectedDevice(), config.webcamResolution(), config.webcamFps());
    }

    public static void updateCapturing(boolean enabled, int device, Resolution resolution, int maxFps) {
        if (enabled && device != -1) {
            if (current == null || current.getDeviceNumber() != device) {
                stopCapturing();
                WebcamClient client = WebcamClient.getInstance();
                try {
                    current = new CapturingDevice(device, resolution, maxFps, client != null && !client.isClosed() ? client.getServerConfig().imageDimension() : 360, f -> Minecraft.getInstance().execute(() -> onFrame(f)));
                    current.start();
                } catch (WebcamException e) {
                    WebcamFabricClient.onWebcamError(e);
                }
            } else {
                current.setResolution(resolution);
                current.setMaxFps(maxFps);
            }
        } else {
            stopCapturing();
        }
    }

    public static void updateImageDimension() {
        WebcamClient client = WebcamClient.getInstance();
        if (client != null && current != null) {
            current.setSquareDimension(client.getServerConfig().imageDimension());
        }
    }

    public static boolean isCapturing() {
        return current != null;
    }

    public static void stopCapturing() {
        if (current != null) {
            current.close();
            Throwable error = current.getError();
            if (error != null) {
                WebcamFabricClient.onWebcamError(error);
            }
            current = null;
        }
    }

    public static void tick() {
        if (current != null) {
            Throwable error = current.getError();
            if (error != null) {
                WebcamFabricClient.onWebcamError(error);
                current.close();
                current = null;
            }
        }
    }

    private static void onFrame(byte[] jpgImage) {
        if (Minecraft.getInstance().screen instanceof WebcamScreen webcamScreen) {
            webcamScreen.onFrame(jpgImage);
        }
        WebcamClient client = WebcamClient.getInstance();
        if (client != null && !client.isClosed() && client.isAuthenticated()) {
            client.sendFrame(jpgImage);
        }
    }

}
