package ru.dimaskama.webcam.neoforge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.LoggerFactory;
import ru.dimaskama.webcam.*;
import ru.dimaskama.webcam.logger.Slf4jLogger;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

@Mod(Webcam.MOD_ID)
public class WebcamNeoForge {

    private static MinecraftServer server;

    public WebcamNeoForge() {
        Webcam.initLogger(new Slf4jLogger(LoggerFactory.getLogger("Webcam")));
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        WebcamMod.init(
                modContainer.getModInfo().getVersion().toString(),
                Path.of("./config/webcam/"),
                new WebcamService() {
                    @Override
                    public <T extends Message> void registerChannel(Channel<T> channel, @Nullable ServerMessaging.ServerHandler<T> handler) {
                        WebcamNeoForgeMessaging.register(channel, handler);
                    }

                    @Override
                    public void sendToPlayer(UUID player, Message message) {
                        MinecraftServer server = WebcamNeoForge.server;
                        if (server != null) {
                            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player);
                            if (serverPlayer != null) {
                                WebcamNeoForgeMessaging.sendToPlayer(serverPlayer, message);
                            }
                        }
                    }

                    @Override
                    public void sendSystemMessage(UUID player, String message) {
                        MinecraftServer server = WebcamNeoForge.server;
                        if (server != null) {
                            server.execute(() -> {
                                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player);
                                if (serverPlayer != null) {
                                    serverPlayer.sendSystemMessage(Component.literal(message));
                                }
                            });
                        }
                    }

                    @Override
                    public void acceptForNearbyPlayers(UUID playerUuid, double maxDistance, Consumer<Set<UUID>> action) {
                        MinecraftServer server = WebcamNeoForge.server;
                        if (server != null) {
                            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                            if (player != null) {
                                Set<UUID> players = new HashSet<>();
                                players.add(player.getUUID());
                                try {
                                    List<Player> levelPlayers = new ArrayList<>(player.level().players());
                                    Vec3 pos = player.position();
                                    double maxDistanceSqr = maxDistance * maxDistance;
                                    for (Player levelPlayer : levelPlayers) {
                                        if (levelPlayer.position().distanceToSqr(pos) <= maxDistanceSqr) {
                                            players.add(levelPlayer.getUUID());
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                                action.accept(players);
                            }
                        }
                    }

                    @Override
                    public boolean checkWebcamBroadcastPermission(UUID playerUuid) {
                        MinecraftServer server = WebcamNeoForge.server;
                        if (server != null) {
                            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                            if (player != null) {
                                return WebcamNeoForgePermissions.checkBroadcast(player);
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean checkWebcamViewPermission(UUID playerUuid) {
                        MinecraftServer server = WebcamNeoForge.server;
                        if (server != null) {
                            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                            if (player != null) {
                                return WebcamNeoForgePermissions.checkView(player);
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean isInReplay() {
                        return false;
                    }
                },
                new WebcamModService() {
                    @Override
                    public boolean isModLoaded(String modId) {
                        return ModList.get().isLoaded(modId);
                    }

                    @Override
                    public boolean checkWebcamconfigCommandPermission(CommandSourceStack commandSource) {
                        return WebcamNeoForgePermissions.checkWebcamconfigCommand(commandSource);
                    }
                }
        );
        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerExtensionPoint(
                    net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                    new ru.dimaskama.webcam.client.neoforge.WebcamNeoForgeConfigScreenFactory()
            );
        }
    }

    static void setServer(MinecraftServer server) {
        WebcamNeoForge.server = server;
    }

}
