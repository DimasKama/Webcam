package ru.dimaskama.webcam.client.cap;

import net.minecraft.network.chat.Component;
import org.lwjgl.system.MemoryUtil;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import ru.dimaskama.webcam.client.config.Resolution;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.opencv.videoio.Videoio.*;

public class CapturingDevice extends Thread {

    private final AtomicBoolean closed = new AtomicBoolean();
    private final int deviceNumber;
    private final FrameConsumer frameConsumer;
    private final Mat tempMat;
    private final Mat tempMat2;
    private volatile Resolution resolution;
    private volatile int fps;
    private volatile int squareDimension;
    private volatile Throwable error;

    public CapturingDevice(int deviceNumber, FrameConsumer frameConsumer) {
        this.deviceNumber = deviceNumber;
        this.frameConsumer = frameConsumer;
        tempMat = new Mat();
        tempMat2 = new Mat();
        setDaemon(true);
        setName("CapturingDevice" + deviceNumber);
        setUncaughtExceptionHandler((t, e) -> error = e);
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setSquareDimension(int squareDimension) {
        this.squareDimension = (squareDimension >> 1) << 1;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public void run() {
        Resolution lastResolution = null;
        int realWidth = 0, realHeight = 0;
        int lastFps = 0;
        int realFps = 0;

        VideoCapture cap = new VideoCapture(deviceNumber);
        Mat mat = new Mat();
        try {
            if (!cap.isOpened()) {
                throw new DeviceException(Component.translatable("webcam.error.device_unavailable", deviceNumber));
            }
            while (!closed.get()) {
                Resolution resolution = this.resolution;
                if (lastResolution != resolution) {
                    lastResolution = resolution;
                    cap.set(CAP_PROP_FRAME_WIDTH, resolution.width);
                    cap.set(CAP_PROP_FRAME_HEIGHT, resolution.height);
                    realWidth = (int) Math.round(cap.get(CAP_PROP_FRAME_WIDTH));
                    realHeight = (int) Math.round(cap.get(CAP_PROP_FRAME_HEIGHT));
                }
                int fps = this.fps;
                if (lastFps != fps) {
                    lastFps = fps;
                    cap.set(CAP_PROP_FPS, fps);
                    realFps = (int) Math.round(cap.get(CAP_PROP_FPS));
                }
                if (!cap.read(mat)) {
                    throw new DeviceException(Component.translatable("webcam.error.device_disconnected", deviceNumber));
                }
                onFrame(realFps, realWidth, realHeight, mat);
            }
        } finally {
            cap.release();
            mat.release();
        }
    }

    private void onFrame(int fps, int width, int height, Mat frame) {
        int minDim;
        // Crop to square
        if (width != height) {
            minDim = Math.min(width, height);
            int startX = (width - minDim) >> 1;
            int startY = (height - minDim) >> 1;
            frame = new Mat(frame, new Rect(startX, startY, minDim, minDim));
        } else {
            minDim = width;
        }
        // Downscale
        int squareDimension = this.squareDimension;
        if (minDim != squareDimension) {
            Imgproc.resize(frame, tempMat, new Size(squareDimension, squareDimension));
            frame = tempMat;
        }
        Imgproc.cvtColor(frame, tempMat2, Imgproc.COLOR_BGR2RGBA);
        frame = tempMat2;
        int rgbaSize = squareDimension * squareDimension * 4;
        byte[] rgba = new byte[rgbaSize];
        MemoryUtil.memByteBuffer(frame.dataAddr(), rgbaSize).get(rgba);
        frameConsumer.consumeFrame(fps, squareDimension, squareDimension, rgba);
    }

    public boolean close() {
        if (closed.compareAndSet(false, true)) {
            tempMat.release();
            tempMat2.release();
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface FrameConsumer {

        void consumeFrame(int fps, int width, int height, byte[] rgba);

    }

}
