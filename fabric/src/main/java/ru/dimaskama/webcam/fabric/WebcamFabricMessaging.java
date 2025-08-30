package ru.dimaskama.webcam.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class WebcamFabricMessaging {

    private static final Map<Channel<?>, CustomPacketPayload.Type<MessagePayload>> CHANNEL_TO_PAYLOAD = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Message> void register(Channel<T> channel, @Nullable ServerMessaging.ServerHandler<T> handler) {
        CustomPacketPayload.Type<MessagePayload> payloadType = new CustomPacketPayload.Type<>(ResourceLocation.parse(channel.getId()));
        CHANNEL_TO_PAYLOAD.put(channel, payloadType);
        (handler != null ? PayloadTypeRegistry.playC2S() : PayloadTypeRegistry.playS2C()).register(
                payloadType,
                StreamCodec.of(
                        (buf, payload) -> payload.message.writeBytes(buf),
                        buf -> new MessagePayload(payloadType, channel.decode(buf))
                )
        );
        if (handler != null) {
            ServerPlayNetworking.registerGlobalReceiver(
                    payloadType,
                    (payload, context) -> handler.handle(
                            context.player().getUUID(),
                            context.player().getGameProfile().getName(),
                            (T) payload.message()
                    )
            );
        } else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                    WebcamFabricMessaging.getPayloadType(Channel.SECRET),
                    (payload, context) ->
                            ru.dimaskama.webcam.client.WebcamModClient.onMessageReceived(payload.message())
            );
        }
    }

    public static CustomPacketPayload.Type<MessagePayload> getPayloadType(Channel<?> channel) {
        CustomPacketPayload.Type<MessagePayload> payloadType = CHANNEL_TO_PAYLOAD.get(channel);
        if (payloadType == null) {
            throw new IllegalStateException("Channel " + channel.getId() + " is not registered");
        }
        return payloadType;
    }

    public static void sendToPlayer(ServerPlayer player, Message message) {
        ServerPlayNetworking.send(player, new MessagePayload(getPayloadType(message.getChannel()), message));
    }

    public record MessagePayload(CustomPacketPayload.Type<MessagePayload> type, Message message) implements CustomPacketPayload {}

}
