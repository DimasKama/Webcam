package ru.dimaskama.webcam.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.dimaskama.webcam.client.DisplayingVideo;
import ru.dimaskama.webcam.client.DisplayingVideoManager;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.net.VideoSource;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {

    @ModifyReturnValue(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at = @At("RETURN")
    )
    private boolean modifyShouldShowName(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        if (!original) {
            return false;
        }
        if (entity instanceof Player) {
            if (DisplayingVideoManager.INSTANCE.hasViewPermission() && WebcamModClient.CONFIG.getData().showWebcams()) {
                DisplayingVideo displayingVideo = DisplayingVideoManager.INSTANCE.get(entity.getUUID());
                if (displayingVideo != null) {
                    DisplayingVideo.RenderData renderData = displayingVideo.getRenderData();
                    return renderData == null || !(renderData.source() instanceof VideoSource.AboveHead aboveHead) || !aboveHead.isHideNickname();
                }
            }
        }
        return true;
    }

}
