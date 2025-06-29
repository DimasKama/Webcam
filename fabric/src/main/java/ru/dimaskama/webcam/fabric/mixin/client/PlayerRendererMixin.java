package ru.dimaskama.webcam.fabric.mixin.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.dimaskama.webcam.fabric.client.WebcamClient;
import ru.dimaskama.webcam.fabric.client.duck.PlayerRenderStateDuck;
import ru.dimaskama.webcam.fabric.client.render.WebcamRenderLayer;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

    private PlayerRendererMixin() {
        super(null, null, 0.0F);
        throw new AssertionError();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addWebcamRenderLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        addLayer(new WebcamRenderLayer<>(this, context.getEntityRenderDispatcher()));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
            at = @At("TAIL")
    )
    private void updateRenderState(AbstractClientPlayer player, PlayerRenderState renderState, float partialTick, CallbackInfo ci) {
        WebcamClient client = WebcamClient.getInstance();
        ((PlayerRenderStateDuck) renderState).webcam_setDisplayingVideo(
                client != null
                        ? client.getDisplayingVideos().get(player.getUUID())
                        : null
        );
    }

}
