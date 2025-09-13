package ru.dimaskama.webcam.mixin.client.fabric.replaymod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.dimaskama.webcam.client.fabric.compat.replay.replaymod.ReplayModCompat;

@Pseudo
@Mixin(targets = "com.replaymod.replay.FullReplaySender", remap = false)
abstract class FullReplaySenderMixin {

    @Shadow(remap = false)
    protected int lastTimeStamp;

    @Inject(method = "doSendPacketsTill", at = @At(value = "HEAD", remap = false), remap = false)
    private void doSendPacketsTillHead(int timestamp, CallbackInfo ci) {
        int delta = (timestamp - this.lastTimeStamp);
        if (delta > 50 || delta < 0) {
            ReplayModCompat.isFastForwarding = true;
        }
    }

    @Inject(method = "doSendPacketsTill", at = @At(value = "RETURN", remap = false), remap = false)
    private void doSendPacketsTillReturn(int timestamp, CallbackInfo ci) {
        ReplayModCompat.isFastForwarding = false;
    }

}
