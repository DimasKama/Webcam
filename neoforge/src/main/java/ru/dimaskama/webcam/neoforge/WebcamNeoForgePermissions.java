package ru.dimaskama.webcam.neoforge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import ru.dimaskama.webcam.Webcam;

@EventBusSubscriber(modid = Webcam.MOD_ID)
public class WebcamNeoForgePermissions {

    public static final PermissionNode<Boolean> BROADCAST = new PermissionNode<>(
            Webcam.MOD_ID,
            "broadcast",
            PermissionTypes.BOOLEAN,
            (player, uuid, contexts) -> true
    );
    public static final PermissionNode<Boolean> VIEW = new PermissionNode<>(
            Webcam.MOD_ID,
            "view",
            PermissionTypes.BOOLEAN,
            (player, uuid, contexts) -> true
    );
    public static final PermissionNode<Boolean> WEBCAMCONFIG_COMMAND = new PermissionNode<>(
            Webcam.MOD_ID,
            "command.config",
            PermissionTypes.BOOLEAN,
            (player, uuid, contexts) -> player != null && player.hasPermissions(2)
    );

    @SubscribeEvent
    private static void onPermissionGatherNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(BROADCAST, VIEW, WEBCAMCONFIG_COMMAND);
    }

    public static boolean checkBroadcast(ServerPlayer player) {
        return PermissionAPI.getPermission(player, BROADCAST);
    }

    public static boolean checkView(ServerPlayer player) {
        return PermissionAPI.getPermission(player, VIEW);
    }

    public static boolean checkWebcamconfigCommand(CommandSourceStack commandSource) {
        ServerPlayer player = commandSource.getPlayer();
        return player != null ? PermissionAPI.getPermission(player, WEBCAMCONFIG_COMMAND) : commandSource.hasPermission(2);
    }

}
