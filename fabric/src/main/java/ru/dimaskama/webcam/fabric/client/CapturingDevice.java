package ru.dimaskama.webcam.fabric.client;

import net.minecraft.network.chat.Component;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import ru.dimaskama.webcam.fabric.client.config.Resolution;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.opencv.videoio.Videoio.*;

public class CapturingDevice extends Thread {

    private final AtomicBoolean closed = new AtomicBoolean();
    private final int deviceNumber;
    private final FrameConsumer frameConsumer;
    private final VideoCapture cap;
    private final Mat tempMat;
    private final MatOfByte tempMatOfByte;
    private Resolution resolution;
    private volatile int realWidth;
    private volatile int realHeight;
    private volatile int maxFps;
    private volatile int squareDimension;
    private volatile Throwable error;
    private volatile boolean running = true;

    public CapturingDevice(int deviceNumber, Resolution resolution, int maxFps, int squareDimension, FrameConsumer frameConsumer) throws WebcamException {
        this.deviceNumber = deviceNumber;
        this.maxFps = maxFps;
        this.squareDimension = squareDimension;
        this.frameConsumer = frameConsumer;
        cap = new VideoCapture(deviceNumber);
        if (!cap.isOpened()) {
            throw new WebcamException(Component.translatable("webcam.error.device_unavailable", deviceNumber));
        }
        setResolution(resolution);
        tempMat = new Mat();
        tempMatOfByte = new MatOfByte();
        setDaemon(true);
        setName("CapturingDevice" + deviceNumber);
        setUncaughtExceptionHandler((t, e) -> error = e);
    }

    public CapturingDevice recreate() throws WebcamException {
        return new CapturingDevice(deviceNumber, resolution, maxFps, squareDimension, frameConsumer);
    }

    public int getDeviceNumber() {
        return deviceNumber;
    }

    public void setResolution(Resolution resolution) {
        if (this.resolution != resolution) {
            synchronized (this) {
                cap.set(CAP_PROP_FRAME_WIDTH, resolution.width);
                cap.set(CAP_PROP_FRAME_HEIGHT, resolution.height);
                realWidth = (int) cap.get(CAP_PROP_FRAME_WIDTH);
                realHeight = (int) cap.get(CAP_PROP_FRAME_HEIGHT);
            }
            this.resolution = resolution;
        }
    }

    public void setMaxFps(int maxFps) {
        this.maxFps = maxFps;
    }

    public void setSquareDimension(int squareDimension) {
        this.squareDimension = squareDimension;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        Mat mat = new Mat();
        while (running) {
            long frameTime = 1000 / maxFps;
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastTime;

            if (elapsedTime >= frameTime) {
                lastTime = now;

                int realWidth, realHeight;

                synchronized (this) {
                    realWidth = this.realWidth;
                    realHeight = this.realHeight;
                    if (!cap.read(mat)) {
                        throw new WebcamException(Component.translatable("webcam.error.device_disconnected", deviceNumber));
                    }
                }

                onFrame(realWidth, realHeight, mat);

            } else {
                try {
                    Thread.sleep(frameTime - elapsedTime);
                } catch (InterruptedException e) {
                    error = e;
                }
            }
        }
    }

    private void onFrame(int realWidth, int realHeight, Mat frame) {
        int minDim;
        // Crop to square
        if (realWidth != realHeight) {
            minDim = Math.min(realWidth, realHeight);
            int startX = (realWidth - minDim) >> 1;
            int startY = (realHeight - minDim) >> 1;
            frame = new Mat(frame, new Rect(startX, startY, minDim, minDim));
        } else {
            minDim = realWidth;
        }
        // Downscale
        int squareDimension = this.squareDimension;
        if (minDim > squareDimension) {
            Imgproc.resize(frame, tempMat, new Size(squareDimension, squareDimension));
            frame = tempMat;
        }
        // Encode to jpg
        if (!Imgcodecs.imencode(".jpg", frame, tempMatOfByte)) {
            throw new WebcamException(Component.translatable("webcam.error.encode_error"));
        }
        frameConsumer.consumeFrame(tempMatOfByte.toArray());
    }

    public void close() {
        running = false;
        if (closed.compareAndSet(false, true)) {
            cap.release();
            tempMat.release();
            tempMatOfByte.release();
        }
    }

    @FunctionalInterface
    public interface FrameConsumer {

        void consumeFrame(byte[] jpgImage);

    }

}
