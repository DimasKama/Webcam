package ru.dimaskama.webcam.client.fabric.compat.replay;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import ru.dimaskama.webcam.client.DisplayingVideoManager;
import ru.dimaskama.webcam.client.KnownSourceClient;
import ru.dimaskama.webcam.client.KnownSourceManager;
import ru.dimaskama.webcam.client.fabric.compat.replay.flashback.FlashbackCompat;
import ru.dimaskama.webcam.client.fabric.compat.replay.replaymod.ReplayModCompat;
import ru.dimaskama.webcam.net.KnownSource;
import ru.dimaskama.webcam.net.packet.*;

// Compatibility with Flashback & ReplayMod
public class ReplaysCompat {

    public static boolean SHOULD_APPLY = FlashbackCompat.IS_FLASHBACK_LOADED;

    public static void init() {
        if (SHOULD_APPLY) {
            PayloadTypeRegistry.playS2C().register(ReplayWebcamMessage.TYPE, ReplayWebcamMessage.STREAM_CODEC);
            ClientPlayNetworking.registerGlobalReceiver(ReplayWebcamMessage.TYPE, ReplaysCompat::onReplayWebcamMessage);

            FlashbackCompat.init();
            ReplayModCompat.init();
        }
    }

    public static void recordPacket(Packet packet) {
        if (SHOULD_APPLY) {
            boolean recordFlashback = FlashbackCompat.shouldRecord();
            boolean recordReplayMod = ReplayModCompat.shouldRecord();
            if (recordFlashback || recordReplayMod) {
                ReplayWebcamMessage replayMessage = switch (packet) {
                    case VideoS2CPacket video -> new ReplayWebcamMessage.Video(video.source(), video.nal());
                    case CloseSourceS2CPacket closeSource -> new ReplayWebcamMessage.CloseSource(closeSource.sourceUuid());
                    case PermissionsS2CPacket permissions -> new ReplayWebcamMessage.ViewPermission(permissions.view());
                    case KnownSourcesS2CPacket knownSources -> new ReplayWebcamMessage.KnownSources(knownSources.sources());
                    default -> null;
                };
                if (replayMessage != null) {
                    if (FlashbackCompat.shouldRecord()) {
                        FlashbackCompat.record(replayMessage);
                    }
                    if (ReplayModCompat.shouldRecord()) {
                        ReplayModCompat.record(replayMessage);
                    }
                }
            }
        }
    }

    public static boolean isInReplay() {
        return SHOULD_APPLY && (FlashbackCompat.isInReplay() || ReplayModCompat.isInReplay());
    }

    private static void onReplayWebcamMessage(ReplayWebcamMessage msg, ClientPlayNetworking.Context context) {
        if (SHOULD_APPLY && (FlashbackCompat.shouldApplyReplayMessage(msg) || ReplayModCompat.shouldApplyReplayMessage(msg))) {
            switch (msg) {
                case ReplayWebcamMessage.Video video ->
                        DisplayingVideoManager.INSTANCE.onVideoPacket(video.source(), video.nal());
                case ReplayWebcamMessage.CloseSource closeSource ->
                        DisplayingVideoManager.INSTANCE.remove(closeSource.sourceUuid());
                case ReplayWebcamMessage.ViewPermission viewPermission ->
                        DisplayingVideoManager.INSTANCE.setViewPermission(viewPermission.view());
                case ReplayWebcamMessage.KnownSources knownSources -> {
                    for (KnownSource knownSource : knownSources.sources()) {
                        KnownSourceManager.INSTANCE.add(new KnownSourceClient(knownSource));
                    }
                }
                default -> {}
            }
        }
    }

}
