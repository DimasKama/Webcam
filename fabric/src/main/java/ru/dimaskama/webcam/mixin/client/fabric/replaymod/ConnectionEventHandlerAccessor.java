package ru.dimaskama.webcam.mixin.client.fabric.replaymod;

import com.replaymod.recording.handler.RecordingEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "com.replaymod.recording.handler.ConnectionEventHandler", remap = false)
public interface ConnectionEventHandlerAccessor {

    @Accessor(value = "recordingEventHandler", remap = false)
    RecordingEventHandler getRecordingEventHandler();

}
