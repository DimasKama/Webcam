package ru.dimaskama.webcam.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.WebcamEvents;
import ru.dimaskama.webcam.WebcamService;
import ru.dimaskama.webcam.fabric.command.WebcamconfigFabricCommand;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class WebcamFabric implements ModInitializer {

    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(Webcam.MOD_ID).orElseThrow();
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        Webcam.init(splitModVersion(MOD_CONTAINER.getMetadata().getVersion().toString()), new WebcamService() {
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
            public void acceptForNearbyPlayers(UUID entity, double maxDistance, Consumer<Set<UUID>> action) {
                MinecraftServer server = WebcamFabric.server;
                if (server != null) {
                    for (ServerLevel level : server.getAllLevels()) {
                        Entity foundEntity = level.getEntity(entity);
                        if (foundEntity != null) {
                            Vec3 pos = foundEntity.position();
                            double maxDistanceSqr = maxDistance * maxDistance;
                            Set<UUID> players = new HashSet<>();
                            for (ServerPlayer player : level.players()) {
                                if (player.position().distanceToSqr(pos) <= maxDistanceSqr) {
                                    players.add(player.getUUID());
                                }
                            }
                            action.accept(players);
                            break;
                        }
                    }
                }
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WebcamFabric.server = server;
            WebcamEvents.onMinecraftServerStarted();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            WebcamEvents.onMinecraftServerStopping();
            WebcamFabric.server = null;
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                WebcamEvents.onPlayerDisconnected(handler.getPlayer().getUUID()));
        CommandRegistrationCallback.EVENT.register(new WebcamconfigFabricCommand());
    }

    private static String splitModVersion(String modVersion) {
        int firstDash = modVersion.indexOf('-');
        return firstDash != -1 ? modVersion.substring(0, firstDash) : modVersion;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Webcam.MOD_ID, path);
    }

}
