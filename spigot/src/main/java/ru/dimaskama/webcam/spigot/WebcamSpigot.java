package ru.dimaskama.webcam.spigot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import ru.dimaskama.webcam.Utils;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.WebcamEvents;
import ru.dimaskama.webcam.WebcamService;
import ru.dimaskama.webcam.command.WebcamconfigCommand;
import ru.dimaskama.webcam.logger.AbstractLogger;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebcamSpigot extends JavaPlugin {

    @Override
    public void onLoad() {
        Logger logger = getLogger();
        Webcam.initLogger(new AbstractLogger() {
            @Override
            public void info(String message) {
                logger.log(Level.INFO, message);
            }

            @Override
            public void info(String message, Throwable e) {
                logger.log(Level.INFO, message, e);
            }

            @Override
            public void warn(String message) {
                logger.log(Level.WARNING, message);
            }

            @Override
            public void warn(String message, Throwable e) {
                logger.log(Level.WARNING, message, e);
            }

            @Override
            public void error(String message) {
                logger.log(Level.SEVERE, message);
            }

            @Override
            public void error(String message, Throwable e) {
                logger.log(Level.SEVERE, message, e);
            }
        });
    }

    @Override
    public void onEnable() {
        Webcam.init(getDescription().getVersion(), new WebcamService() {
            @Override
            public <T extends Message> void registerChannel(Channel<T> channel, ServerMessaging.ServerHandler<T> handler) {
                Messenger messenger = Bukkit.getMessenger();
                if (handler == null) {
                    messenger.registerOutgoingPluginChannel(WebcamSpigot.this, channel.getId());
                } else {
                    messenger.registerIncomingPluginChannel(WebcamSpigot.this, channel.getId(), (ch, player, encoded) ->
                            handler.handle(player.getUniqueId(), channel.decode(ByteBuffer.wrap(encoded))));
                }
            }

            @Override
            public void sendToPlayer(UUID player, Message message) {
                Player serverPlayer = Bukkit.getPlayer(player);
                if (serverPlayer != null) {
                    WebcamSpigot.registerChannel(serverPlayer, message.getChannel());
                    byte[] tempBuf = Utils.TEMP_BUFFERS.get();
                    ByteBuffer buffer = ByteBuffer.wrap(tempBuf);
                    message.writeBytes(buffer);
                    buffer.flip();
                    byte[] encoded = new byte[buffer.remaining()];
                    buffer.get(encoded);
                    serverPlayer.sendPluginMessage(WebcamSpigot.this, message.getChannel().getId(), encoded);
                }
            }

            @Override
            public void acceptForNearbyPlayers(UUID playerUuid, double maxDistance, Consumer<Set<UUID>> action) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    Location pos = player.getLocation();
                    double maxDistanceSqr = maxDistance * maxDistance;
                    Set<UUID> players = new HashSet<>();
                    for (Player levelPlayer : player.getWorld().getPlayers()) {
                        if (levelPlayer.getLocation().distanceSquared(pos) <= maxDistanceSqr) {
                            players.add(levelPlayer.getUniqueId());
                        }
                    }
                    action.accept(players);
                }
            }
        });
        getCommand("webcamconfig").setTabCompleter(this::tabCompleteConfigCommand);
        getServer().getPluginManager().registerEvents(new WebcamSpigotListener(), this);
        WebcamEvents.onMinecraftServerStarted();
    }

    private List<String> tabCompleteConfigCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (command.getName().equalsIgnoreCase(WebcamconfigCommand.COMMAND_NAME)) {
            if (args.length == 1) {
                return WebcamconfigCommand.suggestFields(args[0]).toList();
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        try {
            if (command.getName().equalsIgnoreCase(WebcamconfigCommand.COMMAND_NAME)) {
                if (args.length == 0) {
                    throw new IllegalArgumentException("Expected config field");
                }
                if (args.length == 1) {
                    sender.sendMessage(WebcamconfigCommand.getField(args[0]));
                    return true;
                }
                if (args.length == 2) {
                    sender.sendMessage(WebcamconfigCommand.setField(args[0], args[1]));
                    return true;
                }
                throw new IllegalArgumentException("Too many args");
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c" + e.getLocalizedMessage());
        }
        return false;
    }

    private static void registerChannel(Player player, Channel<?> channel) {
        String channelId = channel.getId();
        if (!player.getListeningPluginChannels().contains(channelId)) {
            try {
                Class<?> craftPlayer = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
                Method addChannel = craftPlayer.getMethod("addChannel", String.class);
                addChannel.invoke(player, channelId);
            } catch (Exception e) {
                Webcam.getLogger().error("Failed to add plugin channel with reflection", e);
            }
        }
    }

    @Override
    public void onDisable() {
        WebcamEvents.onMinecraftServerStopping();
    }

}
