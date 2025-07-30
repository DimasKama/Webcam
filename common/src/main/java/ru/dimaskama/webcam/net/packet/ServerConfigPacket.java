package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;
import ru.dimaskama.webcam.config.SyncedServerConfig;

public record ServerConfigPacket(SyncedServerConfig config) implements Packet {

    public ServerConfigPacket(ByteBuf buf) {
        this(new SyncedServerConfig(buf));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        config.writeBytes(buf);
    }

    @Override
    public int getEstimatedSize() {
        return 8;
    }

    @Override
    public PacketType<ServerConfigPacket> getType() {
        return PacketType.SERVER_CONFIG;
    }

}
