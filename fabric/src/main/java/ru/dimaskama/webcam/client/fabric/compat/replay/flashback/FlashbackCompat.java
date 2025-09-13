package ru.dimaskama.webcam.client.fabric.compat.replay.flashback;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.action.ActionRegistry;
import net.minecraft.client.Minecraft;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.fabric.compat.replay.ReplayWebcamMessage;

public class FlashbackCompat {

    public static final boolean IS_FLASHBACK_LOADED = WebcamMod.getService().isModLoaded("flashback");

    public static void init() {
        if (IS_FLASHBACK_LOADED) {
            Internal.init();
        }
    }

    public static boolean shouldRecord() {
        return IS_FLASHBACK_LOADED && Internal.shouldRecord();
    }

    public static boolean isInReplay() {
        return IS_FLASHBACK_LOADED && Internal.isInReplay();
    }

    public static void record(ReplayWebcamMessage replayMessage) {
        Internal.record(replayMessage);
    }

    public static boolean shouldApplyReplayMessage(ReplayWebcamMessage replayMessage) {
        return isInReplay();
    }

    private static class Internal {

        private static void init() {
            ActionRegistry.register(FlashbackWebcamAction.INSTANCE);
        }

        private static boolean shouldRecord() {
            return Flashback.RECORDER != null && Flashback.RECORDER.readyToWrite();
        }

        private static boolean isInReplay() {
            return Flashback.isInReplay();
        }

        private static void record(ReplayWebcamMessage replayMessage) {
            Minecraft.getInstance().execute(() -> {
                if (shouldRecord()) {
                    Flashback.RECORDER.submitCustomTask(replayWriter -> {
                        replayWriter.startAction(FlashbackWebcamAction.INSTANCE);
                        ReplayWebcamMessage.STREAM_CODEC.encode(replayWriter.friendlyByteBuf(), replayMessage);
                        replayWriter.finishAction(FlashbackWebcamAction.INSTANCE);
                    });
                }
            });
        }

    }

}
