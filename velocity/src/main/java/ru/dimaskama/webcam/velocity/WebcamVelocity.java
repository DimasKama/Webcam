package ru.dimaskama.webcam.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PreTransferEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import org.slf4j.Logger;
import ru.dimaskama.webcam.velocity.config.ProxyConfig;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.file.Path;

@Plugin(
        id = "webcam",
        name = "Webcam",
        version = WebcamVelocityVersion.VERSION,
        authors = "DimasKama",
        description = "Webcam UDP proxy server"
)
public class WebcamVelocity {

    private final ProxyServer proxy;
    private final Logger logger;
    private final ProxyConfig config;
    @Nullable
    private WebcamProxy webcamProxy;

    @Inject
    public WebcamVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.config = new ProxyConfig(dataDirectory.resolve("config.properties"));
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyConfig getConfig() {
        return config;
    }

    @Subscribe
    public EventTask onProxyInitialize(ProxyInitializeEvent event) {
        PluginContainer pluginContainer = proxy.getPluginManager().getPlugin("webcam").orElseThrow();
        logger.info("Using Webcam proxy v{}", pluginContainer.getDescription().getVersion().orElse("???"));
        return EventTask.async(this::loadConfigAndStartWebcamProxy);
    }

    private void loadConfigAndStartWebcamProxy() {
        try {
            config.loadOrCreate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config. Webcam proxy server startup cancelled", e);
        }
        try {
            webcamProxy = new WebcamProxy(this);
            logger.info("Webcam proxy server started on port {}:{}", webcamProxy.getAddress(), webcamProxy.getPort());
            registerChannels();
        } catch (Exception e) {
            if (webcamProxy != null) {
                webcamProxy.close();
                webcamProxy = null;
            }
            throw new RuntimeException("Failed to start Webcam proxy server", e);
        }
    }

    private void registerChannels() {
        proxy.getChannelRegistrar().register(SecretMessage.CHANNEL);
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (event.getIdentifier().equals(SecretMessage.CHANNEL)) {
            WebcamProxy webcamProxy = this.webcamProxy;
            if (webcamProxy != null
                    && event.getSource() instanceof ServerConnection server
                    && event.getTarget() instanceof Player player
            ) {
                SecretMessage secret = null;
                try {
                    secret = new SecretMessage(ByteBuffer.wrap(event.getData()));
                } catch (Exception e) {
                    logger.warn("Failed to decode {} message to {} to proxy the Webcam connection. But anyway denying the packet!", SecretMessage.CHANNEL, player.getGameProfile().getName());
                }
                if (secret != null) {
                    logger.info("Proxying {}'s Webcam connection", player.getGameProfile().getName());
                    SecretMessage modifiedMessage = webcamProxy.onSecretMessage(server.getServer(), player.getUniqueId(), secret);
                    byte[] buffer = new byte[4096];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                    modifiedMessage.writeBytes(byteBuffer);
                    int len = byteBuffer.position();
                    byte[] msg = new byte[len];
                    System.arraycopy(buffer, 0, msg, 0, len);
                    player.sendPluginMessage(SecretMessage.CHANNEL, msg);
                }
            } else {
                logger.warn("Failed to modify Webcam secret message to because Webcam proxy has not started successfully. But anyway denying the packet!");
            }
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        disconnectFromWebcamProxy(event.getPlayer());
    }

    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void onPreTransferEvent(PreTransferEvent event) {
        disconnectFromWebcamProxy(event.player());
    }

    private void disconnectFromWebcamProxy(Player player) {
        WebcamProxy webcamProxy = this.webcamProxy;
        if (webcamProxy != null && webcamProxy.resetBridge(player.getUniqueId())) {
            logger.info("{} disconnected from Webcam proxy", player.getGameProfile().getName());
        }
    }

}
