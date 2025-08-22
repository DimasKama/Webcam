package ru.dimaskama.webcam.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.config.JsonConfig;
import ru.dimaskama.webcam.fabric.WebcamFabric;
import ru.dimaskama.webcam.fabric.WebcamFabricMessaging;
import ru.dimaskama.webcam.fabric.client.compat.IrisCompat;
import ru.dimaskama.webcam.fabric.client.config.BlockedSources;
import ru.dimaskama.webcam.fabric.client.config.ClientConfig;
import ru.dimaskama.webcam.fabric.client.net.WebcamClient;
import ru.dimaskama.webcam.fabric.client.render.WebcamHud;
import ru.dimaskama.webcam.fabric.client.render.WebcamRenderTypes;
import ru.dimaskama.webcam.fabric.client.render.WebcamWorldRenderer;
import ru.dimaskama.webcam.fabric.client.screen.AdvancedWebcamScreen;
import ru.dimaskama.webcam.fabric.client.screen.WebcamScreen;
import ru.dimaskama.webcam.fabric.client.screen.widget.UpdateDevicesButton;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.SecretMessage;
import ru.dimaskama.webcam.message.SecretRequestMessage;

public class WebcamFabricClient implements ClientModInitializer {

    public static final JsonConfig<ClientConfig> CONFIG = new JsonConfig<>(
            FabricLoader.getInstance().getConfigDir().resolve(Webcam.MOD_ID).resolve("client.json").toString(),
            ClientConfig.CODEC,
            ClientConfig::new
    );
    public static final JsonConfig<BlockedSources> BLOCKED_SOURCES = new JsonConfig<>(
            FabricLoader.getInstance().getConfigDir().resolve(Webcam.MOD_ID).resolve("blocked_sources.json").toString(),
            BlockedSources.CODEC,
            BlockedSources::new
    );
    public static final KeyMapping OPEN_WEBCAM_MENU_KEY = new KeyMapping(
            "key.webcam.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.webcam"
    );

    @Override
    public void onInitializeClient() {
        CONFIG.loadOrCreate();
        BLOCKED_SOURCES.loadOrCreate();

        Webcams.init();
        UpdateDevicesButton.updateDevices();

        WebcamRenderTypes.init();
        IrisCompat.init();
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, WebcamFabric.id("webcam_hud"), WebcamHud::drawHud);
        WorldRenderEvents.AFTER_ENTITIES.register(WebcamWorldRenderer::renderWorldWebcams);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                onServerJoin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                onServerDisconnect());
        ClientTickEvents.END_CLIENT_TICK.register(WebcamFabricClient::onClientTick);
        ClientPlayNetworking.registerGlobalReceiver(WebcamFabricMessaging.getPayloadType(Channel.SECRET), (payload, context) ->
                onSecretReceived((SecretMessage) payload.message()));
        KeyBindingHelper.registerKeyBinding(OPEN_WEBCAM_MENU_KEY);
    }

    private static void onServerJoin() {
        var payloadType = WebcamFabricMessaging.getPayloadType(Channel.SECRET_REQUEST);
        if (ClientPlayNetworking.canSend(payloadType)) {
            Webcam.getLogger().info("Sending secret request");
            ClientPlayNetworking.send(new WebcamFabricMessaging.MessagePayload(payloadType, new SecretRequestMessage(Webcam.getVersion())));
        }
    }

    private static void onServerDisconnect() {
        WebcamClient.shutdown();
    }

    private static void onClientTick(Minecraft minecraft) {
        WebcamClient client = WebcamClient.getInstance();
        if (client != null) {
            client.minecraftTick();
        }
        Webcams.updateListeners();
        while (OPEN_WEBCAM_MENU_KEY.consumeClick()) {
            minecraft.setScreen(new WebcamScreen(null, false));
        }
        if (AdvancedWebcamScreen.CAN_USE) {
            AdvancedWebcamScreen.tick(minecraft);
        }
    }

    private static void onSecretReceived(SecretMessage secret) {
        Webcam.getLogger().info("Received secret");
        Minecraft minecraft = Minecraft.getInstance();
        WebcamClient.initialize(minecraft.player.getUUID(), minecraft.getConnection().getConnection().getRemoteAddress(), secret);
    }

}
