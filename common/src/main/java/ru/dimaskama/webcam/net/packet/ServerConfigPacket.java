package ru.dimaskama.webcam.net.packet;

import ru.dimaskama.webcam.config.SyncedServerConfig;

import java.nio.ByteBuffer;

public record ServerConfigPacket(SyncedServerConfig config) implements Packet {

    public ServerConfigPacket(ByteBuffer buffer) {
        this(new SyncedServerConfig(buffer));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        config.writeBytes(buffer);
    }

    @Override
    public PacketType<ServerConfigPacket> getType() {
        return PacketType.SERVER_CONFIG;
    }

}
