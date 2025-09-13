package ru.dimaskama.webcam.mixin.client.fabric.flashback;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.dimaskama.webcam.client.DisplayingVideoManager;

@Pseudo
@Mixin(targets = "com.moulberry.flashback.playback.ReplayServer", remap = false)
abstract class ReplayServerMixin {

    @Shadow(remap = false)
    private int currentTick;

    @Inject(method = "goToReplayTick", at = @At(value = "HEAD", remap = false), remap = false)
    private void goToReplayTickHead(int tick, CallbackInfo ci) {
        if (tick < currentTick) {
            DisplayingVideoManager.INSTANCE.clear();
        }
    }

}
