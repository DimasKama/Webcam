package ru.dimaskama.webcam;

public class Utils {

    public static final ThreadLocal<byte[]> TEMP_BUFFERS = ThreadLocal.withInitial(() -> new byte[4096]);

}
