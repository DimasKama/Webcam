package ru.dimaskama.webcam.client.fabric.compat.replay.replaymod;

import com.replaymod.recording.ReplayModRecording;
import com.replaymod.recording.handler.ConnectionEventHandler;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.fabric.compat.replay.ReplayWebcamMessage;
import ru.dimaskama.webcam.mixin.client.fabric.replaymod.ConnectionEventHandlerAccessor;

public class ReplayModCompat {

    public static final boolean IS_REPLAYMOD_LOADED = WebcamMod.getService().isModLoaded("replaymod");
    public static boolean isInReplay;
    public static boolean isFastForwarding;

    public static void init() {
        if (IS_REPLAYMOD_LOADED) {
            Internal.init();
        }
    }

    public static boolean shouldRecord() {
        return IS_REPLAYMOD_LOADED && Internal.shouldRecord();
    }

    public static boolean isInReplay() {
        return IS_REPLAYMOD_LOADED && isInReplay;
    }

    public static void record(ReplayWebcamMessage replayMessage) {
        Internal.record(replayMessage);
    }

    public static boolean shouldApplyReplayMessage(ReplayWebcamMessage replayMessage) {
        return isInReplay() && (replayMessage.shouldAcceptInFastForwarding() || !isFastForwarding);
    }

    private static class Internal {

        public static void init() {

        }

        private static boolean shouldRecord() {
            return ((ConnectionEventHandlerAccessor) ReplayModRecording.instance.getConnectionEventHandler()).getRecordingEventHandler() != null;
        }

        private static void record(ReplayWebcamMessage replayMessage) {
            ConnectionEventHandler connectionEventHandler = ReplayModRecording.instance.getConnectionEventHandler();
            if (connectionEventHandler != null) {
                connectionEventHandler.getPacketListener().save(new ClientboundCustomPayloadPacket(replayMessage));
            }
        }

    }

}
