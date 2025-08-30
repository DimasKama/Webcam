package ru.dimaskama.webcam.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ru.dimaskama.webcam.*;
import ru.dimaskama.webcam.command.WebcamconfigModCommand;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class WebcamFabric implements ModInitializer {

    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(Webcam.MOD_ID).orElseThrow();
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        WebcamMod.init(
                MOD_CONTAINER.getMetadata().getVersion().toString(),
                FabricLoader.getInstance().getConfigDir().resolve(Webcam.MOD_ID),
                new WebcamService() {
                    @Override
                    public <T extends Message> void registerChannel(Channel<T> channel, @Nullable ServerMessaging.ServerHandler<T> handler) {
                        WebcamFabricMessaging.register(channel, handler);
                    }

                    @Override
                    public void sendToPlayer(UUID player, Message message) {
                        MinecraftServer server = WebcamFabric.server;
                        if (server != null) {
                            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(player);
                            if (serverPlayer != null) {
                                WebcamFabricMessaging.sendToPlayer(serverPlayer, message);
                            }
                        }
                    }

                    @Override
                    public void sendSystemMessage(UUID player, String message) {
                        MinecraftServer server = WebcamFabric.server;
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
                        MinecraftServer server = WebcamFabric.server;
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
                        MinecraftServer server = WebcamFabric.server;
                        if (server != null) {
                            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                            if (player != null) {
                                return Permissions.check(player, Webcam.WEBCAM_BROADCAST_PERMISSION, true);
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean checkWebcamViewPermission(UUID playerUuid) {
                        MinecraftServer server = WebcamFabric.server;
                        if (server != null) {
                            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                            if (player != null) {
                                return Permissions.check(player, Webcam.WEBCAM_VIEW_PERMISSION, true);
                            }
                        }
                        return true;
                    }
                },
                new WebcamModService() {
                    @Override
                    public boolean isModLoaded(String modId) {
                        return FabricLoader.getInstance().isModLoaded(modId);
                    }

                    @Override
                    public boolean checkWebcamconfigCommandPermission(CommandSourceStack commandSource) {
                        return Permissions.check(commandSource, Webcam.WEBCAMCONFIG_COMMAND_PERMISSION, 2);
                    }
                }
        );
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WebcamFabric.server = server;
            WebcamEvents.onMinecraftServerStarted();
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> WebcamEvents.onMinecraftServerTick());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            WebcamEvents.onMinecraftServerStopping();
            WebcamFabric.server = null;
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                WebcamEvents.onPlayerDisconnected(handler.getPlayer().getUUID()));
        CommandRegistrationCallback.EVENT.register((dispatcher, r, e) ->
                WebcamconfigModCommand.register(dispatcher));
    }

}
