package ru.dimaskama.webcam.client.fabric.compat.replay.replaymod;

import com.replaymod.core.Module;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistration;
import com.replaymod.replay.events.ReplayClosingCallback;
import com.replaymod.replay.events.ReplayOpenedCallback;

public class ReplayModWebcamModule implements Module {

    @Override
    public void initClient() {
        EventRegistration.register(ReplayOpenedCallback.EVENT, replayHandler -> ReplayModCompat.isInReplay = true);
        EventRegistration.register(ReplayClosingCallback.EVENT, replayHandler -> ReplayModCompat.isInReplay = false);
    }

}
