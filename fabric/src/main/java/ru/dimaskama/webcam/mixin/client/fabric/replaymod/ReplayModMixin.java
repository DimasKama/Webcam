package ru.dimaskama.webcam.mixin.client.fabric.replaymod;

import com.replaymod.core.Module;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.dimaskama.webcam.client.fabric.compat.replay.replaymod.ReplayModWebcamModule;

import java.util.List;

@Pseudo
@Mixin(targets = "com.replaymod.core.ReplayMod")
abstract class ReplayModMixin {

    @Shadow
    @Final
    private List<Module> modules;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initTail(CallbackInfo ci) {
        modules.add(new ReplayModWebcamModule());
    }

}
