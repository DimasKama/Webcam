package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;
import ru.dimaskama.webcam.net.KnownSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record KnownSourcesS2CPacket(
        List<KnownSource> sources
) implements Packet {

    public KnownSourcesS2CPacket(ByteBuf buf) {
        this(decodeSources(buf));
    }

    private static List<KnownSource> decodeSources(ByteBuf buf) {
        int size = buf.readShort() & 0xFFFF;
        List<KnownSource> sources = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sources.add(new KnownSource(buf));
        }
        return sources;
    }

    public static List<KnownSourcesS2CPacket> split(int mtu, Iterator<KnownSource> iterator) {
        List<KnownSourcesS2CPacket> packets = new ArrayList<>();
        List<KnownSource> sources = new ArrayList<>();
        int packetSize = 2;
        while (iterator.hasNext()) {
            KnownSource source = iterator.next();
            int sourceSize = source.getSize();
            if (packetSize + sourceSize >= mtu && !sources.isEmpty()) {
                packets.add(new KnownSourcesS2CPacket(sources));
                sources = new ArrayList<>();
                packetSize = 2;
            }
            packetSize += sourceSize;
            sources.add(source);
        }
        if (!sources.isEmpty()) {
            packets.add(new KnownSourcesS2CPacket(sources));
        }
        return packets;
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeShort(sources.size());
        for (KnownSource source : sources) {
            source.writeBytes(buf);
        }
    }

    @Override
    public PacketType<KnownSourcesS2CPacket> getType() {
        return PacketType.KNOWN_SOURCES_S2C;
    }

    @Override
    public int getEstimatedSize() {
        return 2 + sources.size() * 17;
    }

}
