package ru.dimaskama.webcam.fabric.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.net.FrameChunk;
import ru.dimaskama.webcam.net.VideoSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DisplayingVideo {

    private final UUID uuid;
    private final ResourceLocation textureId;
    private final List<byte[]> buildingFrame = new ArrayList<>();
    private long sequenceNumber = -1;
    private int frameNumber = -1;
    private FrameChunk.Type frameChunkType = FrameChunk.Type.SINGLE;
    private volatile VideoSource lastSource;
    @Nullable
    private volatile NativeImage newImage;
    private volatile long lastChunkTime = System.currentTimeMillis();
    private DynamicTexture texture;
    private boolean textureUploaded;

    public DisplayingVideo(UUID uuid) {
        this.uuid = uuid;
        textureId = WebcamFabric.id("webcam_" + UndashedUuid.toString(uuid));
    }

    public UUID getUuid() {
        return uuid;
    }

    // Get render data synchronously
    @Nullable
    public RenderData getRenderData() {
        VideoSource source;
        NativeImage newImage;
        synchronized (this) {
            source = lastSource;
            newImage = this.newImage;
            this.newImage = null;
        }
        if (newImage != null) {
            if (texture != null && sizeEquals(texture.getPixels(), newImage)) {
                texture.setPixels(newImage);
                texture.upload();
            } else {
                texture = new DynamicTexture(textureId::getPath, newImage);
                texture.setFilter(true, false);
                Minecraft.getInstance().getTextureManager().register(textureId, texture);
            }
            textureUploaded = true;
        }
        return textureUploaded ? new RenderData(source, textureId) : null;
    }

    private static boolean sizeEquals(NativeImage image1, NativeImage image2) {
        return image1.getWidth() == image2.getWidth() && image1.getHeight() == image2.getHeight();
    }

    public long getLastChunkTime() {
        return lastChunkTime;
    }

    public void closeTexture() {
        Minecraft.getInstance().getTextureManager().release(textureId);
    }

    public void onFrameChunk(VideoSource source, FrameChunk chunk) {
        if (frameNumber != chunk.frameNumber() || sequenceNumber + 1 == chunk.sequenceNumber() && frameChunkType.isNextExpected(chunk.type())) {
            buildingFrame.add(chunk.chunk());
            if (chunk.type() == FrameChunk.Type.END || chunk.type() == FrameChunk.Type.SINGLE) {
                int totalSize = 0;
                for (byte[] savedChunk : buildingFrame) {
                    totalSize += savedChunk.length;
                }
                byte[] jpgImage = new byte[totalSize];
                int i = 0;
                for (byte[] savedChunk : buildingFrame) {
                    System.arraycopy(savedChunk, 0, jpgImage, i, savedChunk.length);
                    i += savedChunk.length;
                }
                buildingFrame.clear();
                try {
                    NativeImage image = ImageUtil.convertJpgToNativeImage(null, jpgImage);
                    // Write render data synchronously
                    synchronized (this) {
                        this.lastSource = source;
                        this.newImage = image;
                    }
                } catch (Exception e) {
                    if (Webcam.isDebugMode()) {
                        Webcam.getLogger().warn(source.getUuid() + " sent invalid jpg frame", e);
                    }
                }
            }
        } else {
            buildingFrame.clear();
        }
        sequenceNumber = chunk.sequenceNumber();
        frameNumber = chunk.frameNumber();
        frameChunkType = chunk.type();
        lastChunkTime = System.currentTimeMillis();
    }

    public record RenderData(VideoSource source, ResourceLocation textureId) {}

}
