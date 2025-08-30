package ru.dimaskama.webcam.mixin.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.dimaskama.webcam.client.render.WebcamRenderLayer;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private PlayerRendererMixin() {
        super(null, null, 0.0F);
        throw new AssertionError();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addWebcamRenderLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        addLayer(new WebcamRenderLayer<>(this, context.getEntityRenderDispatcher()));
    }

}
