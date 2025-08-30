package ru.dimaskama.webcam.client.fabric;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;
import ru.dimaskama.webcam.client.WebcamClientService;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.fabric.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.fabric.WebcamFabricMessaging;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.render.WebcamHud;
import ru.dimaskama.webcam.client.render.WebcamWorldRenderer;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;

public class WebcamFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WebcamModClient.init(new WebcamClientService() {
            @Override
            public boolean canSendToServer(Channel<?> channel) {
                return ClientPlayNetworking.canSend(WebcamFabricMessaging.getPayloadType(channel));
            }

            @Override
            public void sendToServer(Message message) {
                var payloadType = WebcamFabricMessaging.getPayloadType(message.getChannel());
                ClientPlayNetworking.send(new WebcamFabricMessaging.MessagePayload(payloadType, message));
            }

            @Override
            public void tickAdvancedConfigScreen(Minecraft minecraft) {
                AdvancedWebcamScreen.tick(minecraft);
            }

            @Override
            public Screen createAdvancedConfigScreen(@Nullable Screen parent) {
                return new AdvancedWebcamScreen().create(parent);
            }

            @Override
            public RenderType createWebcamRenderType(String name, VertexFormat.Mode mode, ResourceLocation textureId) {
                return RenderType.create(
                        name,
                        DefaultVertexFormat.POSITION_TEX,
                        mode,
                        1536,
                        RenderType.CompositeState.builder()
                                .setTextureState(new RenderStateShard.TextureStateShard(textureId, TriState.TRUE, false))
                                .setShaderState(RenderType.POSITION_TEX_SHADER)
                                .createCompositeState(false)
                );
            }
        });

        KeyBindingHelper.registerKeyBinding(WebcamModClient.OPEN_WEBCAM_MENU_KEY);

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, WebcamMod.id("webcam_hud"), WebcamHud::drawHud));

        WorldRenderEvents.AFTER_ENTITIES.register(context ->
                WebcamWorldRenderer.renderWorldWebcams(context.camera(), context.matrixStack(), context.consumers()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                WebcamModClient.onServerJoinEvent());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                WebcamModClient.onServerDisconnectEvent());

        ClientTickEvents.END_CLIENT_TICK.register(WebcamModClient::onClientTick);
    }

}
