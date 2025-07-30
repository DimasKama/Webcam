package ru.dimaskama.webcam.fabric.client;

import net.minecraft.network.chat.Component;
import ru.dimaskama.javah264.H264Encoder;
import ru.dimaskama.javah264.exception.EncoderException;
import ru.dimaskama.javah264.exception.UnknownPlatformException;

import java.io.IOException;

public class AdaptableH264Encoder {

    private volatile int fps = -1;
    private volatile int mtu = -1;
    private volatile int bitrate = -1;
    private volatile H264Encoder encoder;

    private static H264Encoder createEncoder(int fps, int mtu, int bitrate) {
        try {
            return H264Encoder.builder()
                    .profile(H264Encoder.Profile.Main)
                    .rateControlMode(H264Encoder.RateControlMode.Bitrate)
                    .spsPpsStrategy(H264Encoder.SpsPpsStrategy.IncreasingId)
                    .multipleThreadIdc((short) 1)
                    .maxFrameRate(fps)
                    .maxSliceLen(mtu)
                    .targetBitrate(bitrate * 1000)
                    .intraFramePeriod(3 * fps)
                    .build();
        } catch (IOException | UnknownPlatformException e) {
            throw new RuntimeException("Failed to create H.264 encoder", e);
        }
    }

    public synchronized byte[][] encode(int fps, int mtu, int bitrate, int width, int height, byte[] rgba) throws DeviceException {
        if (this.fps != fps || this.mtu != mtu || this.bitrate != bitrate) {
            if (encoder != null) {
                encoder.close();
            }
            encoder = createEncoder(fps, mtu, bitrate);
            this.fps = fps;
            this.mtu = mtu;
            this.bitrate = bitrate;
        }
        try {
            return encoder.encodeSeparateRGBA(width, height, rgba);
        } catch (EncoderException e) {
            throw new DeviceException(Component.translatable("webcam.error.encode_error"), e);
        }
    }

    public synchronized void close() {
        H264Encoder encoder = this.encoder;
        if (encoder != null) {
            this.encoder = null;
            mtu = -1;
            fps = -1;
            bitrate = -1;
            encoder.close();
        }
    }

}
