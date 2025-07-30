package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class PacketType<T extends Packet> {

    private static final List<PacketType<?>> REGISTRY = new ArrayList<>();

    public static final PacketType<AuthPacket> AUTH = register(AuthPacket::new);
    public static final PacketType<AuthAckPacket> AUTH_ACK = register(b -> AuthAckPacket.INSTANCE);
    public static final PacketType<ServerConfigPacket> SERVER_CONFIG = register(ServerConfigPacket::new);
    public static final PacketType<KeepAlivePacket> KEEP_ALIVE = register(b -> KeepAlivePacket.INSTANCE);
    public static final PacketType<VideoC2SPacket> VIDEO_C2S = register(VideoC2SPacket::new);
    public static final PacketType<VideoS2CPacket> VIDEO_S2C = register(VideoS2CPacket::new);
    public static final PacketType<CloseSourceC2SPacket> CLOSE_SOURCE_C2S = register(b -> CloseSourceC2SPacket.INSTANCE);
    public static final PacketType<CloseSourceS2CPacket> CLOSE_SOURCE_S2C = register(CloseSourceS2CPacket::new);
    public static final PacketType<ShowWebcamsC2SPacket> SHOW_WEBCAMS_C2S = register(ShowWebcamsC2SPacket::new);
    public static final PacketType<AddBlockedSourceC2SPacket> ADD_BLOCKED_SOURCE_C2S = register(AddBlockedSourceC2SPacket::new);
    public static final PacketType<RemoveBlockedSourceC2SPacket> REMOVE_BLOCKED_SOURCE_C2S = register(RemoveBlockedSourceC2SPacket::new);
    public static final PacketType<KnownSourcesS2CPacket> KNOWN_SOURCES_S2C = register(KnownSourcesS2CPacket::new);
    public static final PacketType<PermissionsS2CPacket> PERMISSIONS_S2C = register(PermissionsS2CPacket::decode);

    private final byte id;
    private final Function<ByteBuf, T> decoder;

    private PacketType(byte id, Function<ByteBuf, T> decoder) {
        this.id = id;
        this.decoder = decoder;
    }

    public byte getId() {
        return id;
    }

    public T decode(ByteBuf buf) {
        return decoder.apply(buf);
    }

    public static PacketType<?> byId(byte id) {
        if (id < 0 || id >= REGISTRY.size()) {
            throw new IllegalArgumentException("Unknown packet type with id " + id);
        }
        return REGISTRY.get(id);
    }

    public static <T extends Packet> PacketType<T> register(Function<ByteBuf, T> decoder) {
        int idInt = REGISTRY.size();
        if (idInt > 255) {
            throw new IllegalStateException("Too many packet types registered");
        }
        byte id = (byte) idInt;
        PacketType<T> packetType = new PacketType<>(id, decoder);
        REGISTRY.add(packetType);
        return packetType;
    }

}
