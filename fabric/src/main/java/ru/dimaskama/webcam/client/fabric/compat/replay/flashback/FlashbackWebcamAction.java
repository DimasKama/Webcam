package ru.dimaskama.webcam.client.fabric.compat.replay.flashback;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.action.Action;
import com.moulberry.flashback.playback.ReplayPlayer;
import com.moulberry.flashback.playback.ReplayServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.fabric.compat.replay.ReplayWebcamMessage;

public class FlashbackWebcamAction implements Action {

    public static final FlashbackWebcamAction INSTANCE = new FlashbackWebcamAction();
    private static final ResourceLocation NAME = WebcamMod.id("action/webcam");

    @Override
    public ResourceLocation name() {
        return NAME;
    }

    @Override
    public void handle(ReplayServer replayServer, RegistryFriendlyByteBuf buf) {
        ReplayWebcamMessage replayMessage = ReplayWebcamMessage.STREAM_CODEC.decode(buf);
        if (!replayServer.isProcessingSnapshot && (Flashback.isExporting() || replayMessage.shouldAcceptInFastForwarding() || !replayServer.fastForwarding)) {
            for (ReplayPlayer replayViewer : replayServer.getReplayViewers()) {
                ServerPlayNetworking.send(replayViewer, replayMessage);
            }
        }
    }

}
