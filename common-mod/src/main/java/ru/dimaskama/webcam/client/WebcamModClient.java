package ru.dimaskama.webcam.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.glfw.GLFW;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.cap.Capturing;
import ru.dimaskama.webcam.client.net.WebcamClient;
import ru.dimaskama.webcam.client.screen.WebcamScreen;
import ru.dimaskama.webcam.config.JsonConfig;
import ru.dimaskama.webcam.client.config.BlockedSources;
import ru.dimaskama.webcam.client.config.ClientConfig;
import ru.dimaskama.webcam.client.render.WebcamRenderTypes;
import ru.dimaskama.webcam.client.screen.widget.UpdateDevicesButton;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.SecretMessage;
import ru.dimaskama.webcam.message.SecretRequestMessage;

import java.nio.file.Path;

public class WebcamModClient {

    public static final JsonConfig<ClientConfig> CONFIG = new JsonConfig<>(
            Path.of("./config/webcam/client.json").toString(),
            ClientConfig.CODEC,
            ClientConfig::new
    );
    public static final JsonConfig<BlockedSources> BLOCKED_SOURCES = new JsonConfig<>(
            Path.of("./config/webcam/blocked_sources.json").toString(),
            BlockedSources.CODEC,
            BlockedSources::new
    );
    public static final KeyMapping OPEN_WEBCAM_MENU_KEY = new KeyMapping(
            "key.webcam.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.webcam"
    );
    private static WebcamClientService service;
    private static boolean canUseAdvancedConfigScreen;

    public static void init(WebcamClientService service) {
        WebcamModClient.service = service;
        canUseAdvancedConfigScreen = WebcamMod.getService().isModLoaded("cloth-config")
                || WebcamMod.getService().isModLoaded("cloth_config");

        CONFIG.loadOrCreate();
        BLOCKED_SOURCES.loadOrCreate();

        Capturing.init();
        UpdateDevicesButton.updateDevices();

        WebcamRenderTypes.init();
    }

    public static WebcamClientService getService() {
        return service;
    }

    public static boolean canUseAdvancedConfigScreen() {
        return canUseAdvancedConfigScreen;
    }

    public static void onServerJoinEvent() {
        if (!Webcam.getService().isInReplay() && service.canSendToServer(Channel.SECRET_REQUEST)) {
            Webcam.getLogger().info("Sending secret request");
            service.sendToServer(new SecretRequestMessage(Webcam.getVersion()));
        }
    }

    public static void onServerDisconnectEvent() {
        WebcamClient.shutdown();
        DisplayingVideoManager.INSTANCE.clear();
        KnownSourceManager.INSTANCE.clear();
    }

    public static void onClientTick(Minecraft minecraft) {
        WebcamClient client = WebcamClient.getInstance();
        if (client != null) {
            client.minecraftTick();
        }
        Capturing.updateListeners();
        while (OPEN_WEBCAM_MENU_KEY.consumeClick()) {
            minecraft.setScreen(new WebcamScreen(null, false));
        }
        if (canUseAdvancedConfigScreen()) {
            service.tickAdvancedConfigScreen(minecraft);
        }
    }

    public static void onClientLevelTick(ClientLevel level) {
        if (level.tickRateManager().runsNormally()) {
            DisplayingVideoManager.INSTANCE.levelTick();
        }
    }

    public static void onMessageReceived(Message message) {
        if (message instanceof SecretMessage secretMessage) {
            onSecretMessageReceived(secretMessage);
            return;
        }
        throw new IllegalStateException("Can't handle message " + message + " of channel " + message.getChannel().getId() + " on client");
    }

    private static void onSecretMessageReceived(SecretMessage secret) {
        if (!Webcam.getService().isInReplay()) {
            Webcam.getLogger().info("Received Webcam secret");
            Minecraft minecraft = Minecraft.getInstance();
            WebcamClient.initialize(minecraft.player.getUUID(), minecraft.getConnection().getConnection().getRemoteAddress(), secret);
        }
    }

}
