package ru.dimaskama.webcam.neoforge;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.message.Channel;
import ru.dimaskama.webcam.message.Message;
import ru.dimaskama.webcam.message.ServerMessaging;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Webcam.MOD_ID)
public class WebcamNeoForgeMessaging {

    private static final List<ChannelToRegister<?>> channelsToRegister = new ArrayList<>();
    private static final Map<Channel<?>, CustomPacketPayload.Type<MessagePayload>> CHANNEL_TO_PAYLOAD = new HashMap<>();

    public static <T extends Message> void register(Channel<T> channel, @Nullable ServerMessaging.ServerHandler<T> handler) {
        CHANNEL_TO_PAYLOAD.put(channel, new CustomPacketPayload.Type<>(ResourceLocation.parse(channel.getId())));
        channelsToRegister.add(new ChannelToRegister<>(channel, handler));
    }

    @SubscribeEvent
    private static void onRegisterPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN)
                .optional();
        for (ChannelToRegister<?> channelToRegister : channelsToRegister) {
            registerPayloadChannel(registrar, channelToRegister);
        }
        channelsToRegister.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Message> void registerPayloadChannel(PayloadRegistrar registrar, ChannelToRegister<T> channelToRegister) {
        Channel<T> channel = channelToRegister.channel;
        ServerMessaging.ServerHandler<T> handler = channelToRegister.handler;
        CustomPacketPayload.Type<MessagePayload> payloadType = getPayloadType(channel);
        StreamCodec<ByteBuf, MessagePayload> streamCodec = StreamCodec.of(
                (buf, payload) -> payload.message.writeBytes(buf),
                buf -> new MessagePayload(payloadType, channel.decode(buf))
        );
        if (handler != null) {
            registrar.playToServer(
                    payloadType,
                    streamCodec,
                    (payload, context) ->
                            handler.handle(context.player().getUUID(), context.player().getGameProfile().getName(), (T) payload.message)
            );
        } else {
            registrar.playToClient(
                    payloadType,
                    streamCodec,
                    (payload, context) ->
                            ru.dimaskama.webcam.client.WebcamModClient.onMessageReceived(payload.message)
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
        PacketDistributor.sendToPlayer(player, new MessagePayload(getPayloadType(message.getChannel()), message));
    }

    public record MessagePayload(CustomPacketPayload.Type<MessagePayload> type, Message message) implements CustomPacketPayload {}

    private record ChannelToRegister<T extends Message>(Channel<T> channel, @Nullable ServerMessaging.ServerHandler<T> handler) {}

}
