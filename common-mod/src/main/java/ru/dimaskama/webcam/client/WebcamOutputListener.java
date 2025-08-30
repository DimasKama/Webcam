package ru.dimaskama.webcam.client;

import ru.dimaskama.webcam.client.config.Resolution;

import javax.annotation.Nullable;

public interface WebcamOutputListener extends Comparable<WebcamOutputListener> {

    int getSelectedDevice();

    @Nullable
    Resolution getResolution();

    int getFps();

    int getImageDimension();

    boolean isListeningFrames();

    void onFrame(int deviceNumber, int fps, int width, int height, byte[] rgba);

    void onError(DeviceException e);

    default int getPriority() {
        return 0;
    }

    @Override
    default int compareTo(WebcamOutputListener o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

}
