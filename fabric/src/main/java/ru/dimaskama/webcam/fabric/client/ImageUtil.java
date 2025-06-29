package ru.dimaskama.webcam.fabric.client;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.system.MemoryUtil;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.dimaskama.webcam.fabric.mixin.client.NativeImageAccessor;

import javax.annotation.Nullable;

public class ImageUtil {

    private static final ThreadLocal<TempBuffers> TEMP_BUFFERS = ThreadLocal.withInitial(TempBuffers::create);

    public static NativeImage convertJpgToNativeImage(@Nullable NativeImage oldImage, byte[] jpgImage) throws IllegalArgumentException {
        TempBuffers tempBuffers = TEMP_BUFFERS.get();
        Mat bgrImage = null;
        try {
            tempBuffers.jpgImage.fromArray(jpgImage);
            bgrImage = Imgcodecs.imdecode(tempBuffers.jpgImage, Imgcodecs.IMREAD_COLOR);
            if (bgrImage.dataAddr() == 0L) {
                throw new IllegalArgumentException("Failed to decode jpg image");
            }
            Imgproc.cvtColor(bgrImage, tempBuffers.rgbaImage, Imgproc.COLOR_BGR2RGBA);
        } finally {
            if (bgrImage != null) {
                bgrImage.release();
            }
        }
        long memoryAddress = tempBuffers.rgbaImage.dataAddr();
        NativeImage image;
        if (oldImage != null
                && oldImage.format() == NativeImage.Format.RGBA
                && oldImage.getWidth() == tempBuffers.rgbaImage.cols()
                && oldImage.getHeight() == tempBuffers.rgbaImage.rows()
        ) {
            image = oldImage;
        } else {
            image = new NativeImage(NativeImage.Format.RGBA, tempBuffers.rgbaImage.cols(), tempBuffers.rgbaImage.rows(), false);
        }
        MemoryUtil.memCopy(memoryAddress, ((NativeImageAccessor) (Object) image).webcam_getPixelsAddress(), tempBuffers.rgbaImage.total() * tempBuffers.rgbaImage.elemSize());
        return image;
    }

    private record TempBuffers(MatOfByte jpgImage, Mat rgbaImage) {

        public static TempBuffers create() {
            return new TempBuffers(new MatOfByte(), new Mat());
        }

    }

}
