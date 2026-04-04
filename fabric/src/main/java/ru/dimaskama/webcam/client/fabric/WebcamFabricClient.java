package ru.dimaskama.webcam.client.fabric;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import ru.dimaskama.webcam.client.WebcamClientService;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.fabric.compat.replay.ReplaysCompat;
import ru.dimaskama.webcam.client.fabric.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.fabric.WebcamFabricMessaging;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.render.WebcamHud;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.net.packet.Packet;

import javax.annotation.Nullable;

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
            public RenderType createWebcamRenderType(String name, RenderPipeline renderPipeline, Identifier textureId) {
                return RenderType.create(
                        name,
                        RenderSetup.builder(renderPipeline)
                                .withTexture("Sampler0", textureId, () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR))
                                .createRenderSetup()
                );
            }

            @Override
            public void recordPacket(Packet packet) {
                ReplaysCompat.recordPacket(packet);
            }
        });

        KeyMappingHelper.registerKeyMapping(WebcamModClient.OPEN_WEBCAM_MENU_KEY);

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, WebcamMod.id("webcam_hud"), WebcamHud::drawHud);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                WebcamModClient.onServerJoinEvent());

        ClientTickEvents.END_CLIENT_TICK.register(WebcamModClient::onClientTick);

        ClientTickEvents.END_LEVEL_TICK.register(WebcamModClient::onClientLevelTick);

        ReplaysCompat.init();
    }

}
