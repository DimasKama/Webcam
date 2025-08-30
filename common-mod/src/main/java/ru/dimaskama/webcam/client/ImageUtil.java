package ru.dimaskama.webcam.client;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.system.MemoryUtil;
import ru.dimaskama.webcam.mixin.client.NativeImageAccessor;

import javax.annotation.Nullable;

public class ImageUtil {

    public static NativeImage createNativeImage(@Nullable NativeImage oldImage, int width, int height, byte[] rgba) {
        NativeImage image;
        if (oldImage != null
                && oldImage.format() == NativeImage.Format.RGBA
                && oldImage.getWidth() == width
                && oldImage.getHeight() == height
        ) {
            image = oldImage;
        } else {
            image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        }
        MemoryUtil.memByteBuffer(((NativeImageAccessor) (Object) image).webcam_getPixelsAddress(), rgba.length).put(rgba);
        return image;
    }

}
