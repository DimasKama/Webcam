package ru.dimaskama.webcam.net.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PacketType<T extends Packet> {

    private static final List<PacketType<?>> REGISTRY = new ArrayList<>();

    public static final PacketType<AuthPacket> AUTH = register(15000L, AuthPacket::new);
    public static final PacketType<AuthAckPacket> AUTH_ACK = register(15000L, b -> AuthAckPacket.INSTANCE);
    public static final PacketType<ServerConfigPacket> SERVER_CONFIG = register(15000L, ServerConfigPacket::new);
    public static final PacketType<KeepAlivePacket> KEEP_ALIVE = register(5000L, b -> KeepAlivePacket.INSTANCE);
    public static final PacketType<FrameChunkC2SPacket> FRAME_CHUNK_C2S = register(1000L, FrameChunkC2SPacket::new);
    public static final PacketType<FrameChunkS2CPacket> FRAME_CHUNK_S2C = register(1000L, FrameChunkS2CPacket::new);
    public static final PacketType<CloseSourceC2SPacket> CLOSE_SOURCE_C2S = register(1000L, b -> CloseSourceC2SPacket.INSTANCE);
    public static final PacketType<CloseSourceS2CPacket> CLOSE_SOURCE_S2C = register(1000L, CloseSourceS2CPacket::new);

    private final byte id;
    private final long ttl;
    private final Function<ByteBuffer, T> decoder;

    private PacketType(byte id, long ttl, Function<ByteBuffer, T> decoder) {
        this.id = id;
        this.ttl = ttl;
        this.decoder = decoder;
    }

    public byte getId() {
        return id;
    }

    public long getTTL() {
        return ttl;
    }

    public T decode(ByteBuffer buffer) {
        return decoder.apply(buffer);
    }

    public static PacketType<?> byId(byte id) {
        if (id < 0 || id >= REGISTRY.size()) {
            throw new IllegalArgumentException("Unknown packet type with id " + id);
        }
        return REGISTRY.get(id);
    }

    public static <T extends Packet> PacketType<T> register(long ttl, Function<ByteBuffer, T> decoder) {
        int idInt = REGISTRY.size();
        if (idInt > 255) {
            throw new IllegalStateException("Too many packet types registered");
        }
        byte id = (byte) idInt;
        PacketType<T> packetType = new PacketType<>(id, ttl, decoder);
        REGISTRY.add(packetType);
        return packetType;
    }

}
