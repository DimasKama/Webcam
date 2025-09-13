package ru.dimaskama.webcam.client;

import net.minecraft.client.Minecraft;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.net.NalUnit;
import ru.dimaskama.webcam.net.VideoSource;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DisplayingVideoManager {

    public static final DisplayingVideoManager INSTANCE = new DisplayingVideoManager();
    private final Map<UUID, DisplayingVideo> uuidToVideo = new ConcurrentHashMap<>();
    private volatile boolean viewPermission = true;
    private long minecraftTickCount;

    public void setViewPermission(boolean view) {
        boolean prevView = viewPermission;
        viewPermission = view;
        if (prevView && !view) {
            clear();
        }
    }

    public boolean hasViewPermission() {
        return viewPermission;
    }

    public VideoPacketResponse onVideoPacket(VideoSource source, NalUnit nal) {
        if (viewPermission) {
            if (WebcamModClient.CONFIG.getData().showWebcams()) {
                if (!WebcamModClient.BLOCKED_SOURCES.getData().contains(source.getUuid())) {
                    DisplayingVideo displayingVideo = uuidToVideo.computeIfAbsent(source.getUuid(), DisplayingVideo::new);
                    displayingVideo.onVideoPacket(minecraftTickCount, source, nal);
                    return VideoPacketResponse.ACCEPTED;
                }
                return VideoPacketResponse.SOURCE_IS_BLOCKED;
            }
            return VideoPacketResponse.WEBCAMS_ARE_DISABLED;
        }
        return VideoPacketResponse.NO_PERMISSION;
    }

    @Nullable
    public DisplayingVideo get(UUID sourceUuid) {
        return uuidToVideo.get(sourceUuid);
    }

    public void remove(UUID sourceUuid) {
        DisplayingVideo displayingVideo = uuidToVideo.remove(sourceUuid);
        if (displayingVideo != null) {
            displayingVideo.close();
        }
    }

    public void forEach(Consumer<DisplayingVideo> consumer) {
        uuidToVideo.values().forEach(consumer);
    }

    public void clear() {
        Minecraft.getInstance().execute(() ->
                uuidToVideo.values().removeIf(displayingVideo -> {
                    displayingVideo.close();
                    return true;
                })
        );
    }

    public void levelTick() {
        long time = minecraftTickCount++;
        uuidToVideo.values().removeIf(displayingVideo -> {
            if (time - displayingVideo.getLastChunkTime() > 100L) {
                displayingVideo.close();
                Webcam.getLogger().info("Removing displaying video " + displayingVideo.getUuid() + " as it was inactive for 5s");
                return true;
            }
            return false;
        });
    }

    public enum VideoPacketResponse {

        ACCEPTED,
        NO_PERMISSION,
        WEBCAMS_ARE_DISABLED,
        SOURCE_IS_BLOCKED

    }

}
