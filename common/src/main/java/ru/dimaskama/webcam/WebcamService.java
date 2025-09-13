package ru.dimaskama.webcam;

import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface WebcamService {

    <T extends Message> void registerChannel(Channel<T> channel, @Nullable ServerMessaging.ServerHandler<T> handler);

    void sendToPlayer(UUID player, Message message);

    void sendSystemMessage(UUID player, String message);

    void acceptForNearbyPlayers(UUID playerUuid, double maxDistance, Consumer<Set<UUID>> action);

    boolean checkWebcamBroadcastPermission(UUID playerUuid);

    boolean checkWebcamViewPermission(UUID playerUuid);

    boolean isInReplay();

}
