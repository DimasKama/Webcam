package ru.dimaskama.webcam.fabric.client;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.network.chat.Component;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.fabric.client.config.Resolution;

public class Webcams {

    private static final ObjectSortedSet<WebcamOutputListener> LISTENERS = ObjectSortedSets.synchronize(new ObjectLinkedOpenHashSet<>());
    private static final Int2ObjectMap<CapturingDevice> CAPTURING_DEVICES = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static volatile IntList devices = IntList.of();
    private static volatile boolean updatingDevices;

    public static void init() {
        Webcam.getLogger().info("Loading OpenCV...");
        OpenCV.loadLocally();
        if (Webcam.isDebugMode()) {
            Webcam.getLogger().info("OpenCV build information: " + Core.getBuildInformation());
        }
    }

    // OpenCV has no function to list available devices, so we have to try to start VideoCapture on each index
    public static synchronized void updateDevices() {
        updatingDevices = true;
        Webcam.getLogger().info("Updating available webcam devices. Some errors may be printed");
        CAPTURING_DEVICES.values().removeIf(d -> {
            d.close();
            return true;
        });
        devices = IntLists.unmodifiable(listDevices());
        updatingDevices = false;
    }

    private static IntList listDevices() {
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
        return list;
    }

    public static IntList getDevices() {
        return devices;
    }

    public static void addListener(WebcamOutputListener listener) {
        LISTENERS.add(listener);
    }

    public static void removeListener(WebcamOutputListener listener) {
        LISTENERS.remove(listener);
    }

    public static boolean isCapturing(int deviceNumber) {
        CapturingDevice device = CAPTURING_DEVICES.get(deviceNumber);
        return device != null && device.getError() == null;
    }

    public static void broadcastError(Component text) {
        DeviceException exception = new DeviceException(text);
        LISTENERS.forEach(listener -> listener.onError(exception));
    }

    public static void updateListeners() {
        if (!updatingDevices) {
            updateListenersInternal();
        }
    }

    private static synchronized void updateListenersInternal() {
        if (!updatingDevices) {
            for (int deviceNumber : devices) {
                int frameListenerCount = 0;
                Resolution resolution = Resolution.R_1280X720;
                int fps = 30;
                int imageDimension = 360;
                synchronized (LISTENERS) {
                    for (WebcamOutputListener listener : LISTENERS) {
                        if (deviceNumber == listener.getSelectedDevice()) {
                            if (listener.isListeningFrames()) {
                                ++frameListenerCount;
                            }
                            Resolution resolutionOverride = listener.getResolution();
                            if (resolutionOverride != null) {
                                resolution = resolutionOverride;
                            }
                            int fpsOverride = listener.getFps();
                            if (fpsOverride != -1) {
                                fps = fpsOverride;
                            }
                            int imageDimensionOverride = listener.getImageDimension();
                            if (imageDimensionOverride != -1) {
                                imageDimension = imageDimensionOverride;
                            }
                        }
                    }
                }
                if (frameListenerCount != 0) {
                    CapturingDevice device = CAPTURING_DEVICES.computeIfAbsent(deviceNumber, i -> new CapturingDevice(i, new FrameConsumerImpl(i)));
                    device.setResolution(resolution);
                    device.setFps(fps);
                    device.setSquareDimension(imageDimension);
                    if (device.getState() == Thread.State.NEW) {
                        device.start();
                    } else {
                        Throwable error = device.getError();
                        if (error != null && device.close()) {
                            DeviceException deviceException = DeviceException.wrap(deviceNumber, error);
                            LISTENERS.forEach(listener -> {
                                if (listener.getSelectedDevice() == deviceNumber) {
                                    listener.onError(deviceException);
                                }
                            });
                            Webcam.getLogger().warn("Device error", deviceException);
                        }
                    }
                } else {
                    CapturingDevice device = CAPTURING_DEVICES.remove(deviceNumber);
                    if (device != null) {
                        device.close();
                    }
                }
            }
        }
    }

    private static class FrameConsumerImpl implements CapturingDevice.FrameConsumer {

        private final int deviceNumber;

        private FrameConsumerImpl(int deviceNumber) {
            this.deviceNumber = deviceNumber;
        }

        @Override
        public void consumeFrame(int fps, int width, int height, byte[] rgba) {
            LISTENERS.forEach(listener -> {
                if (deviceNumber == listener.getSelectedDevice() && listener.isListeningFrames()) {
                    listener.onFrame(deviceNumber, fps, width, height, rgba);
                }
            });
        }

    }

}
