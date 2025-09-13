package ru.dimaskama.webcam.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.dimaskama.webcam.client.DisplayingVideoManager;
import ru.dimaskama.webcam.client.KnownSourceManager;
import ru.dimaskama.webcam.client.net.WebcamClient;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        WebcamClient.shutdown();
        DisplayingVideoManager.INSTANCE.clear();
        KnownSourceManager.INSTANCE.clear();
    }

}
