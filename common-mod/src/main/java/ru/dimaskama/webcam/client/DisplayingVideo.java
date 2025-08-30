package ru.dimaskama.webcam.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.javah264.DecodeResult;
import ru.dimaskama.javah264.H264Decoder;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.net.NalUnit;
import ru.dimaskama.webcam.net.VideoSource;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DisplayingVideo {

    private final UUID uuid;
    private final ResourceLocation textureId;
    private final H264Decoder decoder;
    private final VideoPacketBuffer buffer = new VideoPacketBuffer(WebcamModClient.CONFIG.getData().packetBufferSize(), this::acceptVideoPacket);
    private final AtomicReference<DecodeResult> newFrame = new AtomicReference<>();
    private volatile VideoSource lastSource;
    private volatile long lastChunkTime = System.currentTimeMillis();
    private NativeImage image;
    private DynamicTexture texture;

    public DisplayingVideo(UUID uuid) {
        this.uuid = uuid;
        textureId = WebcamMod.id("webcam_" + UndashedUuid.toString(uuid));
        try {
            decoder = H264Decoder.builder()
                    .flushBehavior(H264Decoder.FlushBehavior.NoFlush)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create H264Decoder", e);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public RenderData getRenderData() {
        DecodeResult newFrame = this.newFrame.getAndSet(null);
        if (newFrame != null) {
            NativeImage image = ImageUtil.createNativeImage(this.image, newFrame.getWidth(), newFrame.getHeight(), newFrame.getImage());
            if (image != this.image) {
                if (texture != null) {
                    texture.close();
                }
                texture = new DynamicTexture(textureId::getPath, image);
                texture.setFilter(true, false);
                Minecraft.getInstance().getTextureManager().register(textureId, texture);
            } else {
                texture.upload();
            }
            this.image = image;
        }
        return texture != null ? new RenderData(lastSource, textureId) : null;
    }

    private static boolean sizeEquals(NativeImage image1, NativeImage image2) {
        return image1.getWidth() == image2.getWidth() && image1.getHeight() == image2.getHeight();
    }

    public long getLastChunkTime() {
        return lastChunkTime;
    }

    public void onVideoPacket(VideoSource source, NalUnit nalUnit) {
        buffer.receivePacket(nalUnit.sequenceNumber(), nalUnit.data());
        lastSource = source;
        lastChunkTime = System.currentTimeMillis();
    }

    private void acceptVideoPacket(byte[] nal) {
        DecodeResult decoded;
        synchronized (decoder) {
            decoded = decoder.decodeRGBA(nal);
        }
        if (decoded != null) {
            newFrame.set(decoded);
        }
    }

    public void close() {
        Minecraft.getInstance().getTextureManager().release(textureId);
    }

    public record RenderData(VideoSource source, ResourceLocation textureId) {}

}
