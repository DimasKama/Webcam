package ru.dimaskama.webcam.client.neoforge;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.client.WebcamClientService;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.neoforge.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.client.render.WebcamHud;
import ru.dimaskama.webcam.client.render.WebcamWorldRenderer;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.neoforge.WebcamNeoForgeMessaging;
import ru.dimaskama.webcam.net.packet.Packet;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Webcam.MOD_ID, value = Dist.CLIENT)
public class WebcamNeoForgeClientEvents {

    @SubscribeEvent
    private static void onClientStartedEvent(ClientStartedEvent event) {
        WebcamModClient.init(new WebcamClientService() {
            @Override
            public boolean canSendToServer(Channel<?> channel) {
                return Minecraft.getInstance().getConnection().hasChannel(WebcamNeoForgeMessaging.getPayloadType(channel));
            }

            @Override
            public void sendToServer(Message message) {
                ClientPacketDistributor.sendToServer(new WebcamNeoForgeMessaging.MessagePayload(WebcamNeoForgeMessaging.getPayloadType(message.getChannel()), message));
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
            public RenderType createWebcamRenderType(String name, RenderPipeline renderPipeline, ResourceLocation textureId) {
                return RenderType.create(
                        name,
                        1536,
                        renderPipeline,
                        RenderType.CompositeState.builder()
                                .setTextureState(new RenderStateShard.TextureStateShard(textureId, false))
                                .createCompositeState(false)
                );
            }

            @Override
            public void recordPacket(Packet packet) {

            }
        });
    }

    @SubscribeEvent
    private static void onRegisterKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(WebcamModClient.OPEN_WEBCAM_MENU_KEY);
    }

    @SubscribeEvent
    private static void onRenderGuiLayerEvent(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) {
            WebcamHud.drawHud(event.getGuiGraphics(), Minecraft.getInstance().getDeltaTracker());
        }
    }

    @SubscribeEvent
    private static void onRenderAfterEntitiesEvent(RenderLevelStageEvent.AfterEntities event) {
        WebcamWorldRenderer.renderWorldWebcams(event.getCamera(), event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource());
    }

    @SubscribeEvent
    private static void onClientPlayerLoggingInEvent(ClientPlayerNetworkEvent.LoggingIn event) {
        WebcamModClient.onServerJoinEvent();
    }

    @SubscribeEvent
    private static void onClientPlayerLoggingOutEvent(ClientPlayerNetworkEvent.LoggingOut event) {
        WebcamModClient.onServerDisconnectEvent();
    }

    @SubscribeEvent
    private static void onClientTickEvent(ClientTickEvent.Post event) {
        WebcamModClient.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    private static void onLevelTickEvent(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ClientLevel clientLevel) {
            WebcamModClient.onClientLevelTick(clientLevel);
        }
    }

}
