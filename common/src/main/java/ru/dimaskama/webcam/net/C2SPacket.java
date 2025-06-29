package ru.dimaskama.webcam.net;

import ru.dimaskama.webcam.net.packet.Packet;
import ru.dimaskama.webcam.server.PlayerState;
import ru.dimaskama.webcam.server.WebcamServer;

import java.nio.ByteBuffer;
import java.util.UUID;

public record C2SPacket(
        PlayerState sender,
        Packet packet
) {

    public static byte MAGIC = (byte) 0b11001100;

    public static byte[] encrypt(UUID sender, UUID secret, Packet packet) throws Exception {
        byte[] encrypted = packet.encrypt(secret);
        byte[] result = new byte[17 + encrypted.length];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer
                .put(MAGIC)
                .putLong(sender.getMostSignificantBits())
                .putLong(sender.getLeastSignificantBits())
                .put(encrypted);
        return result;
    }

    public static C2SPacket decrypt(WebcamServer server, byte[] data) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if (buffer.get() != MAGIC) {
            throw new IllegalArgumentException("Invalid packet");
        }
        UUID sender = new UUID(buffer.getLong(), buffer.getLong());
        PlayerState player = server.getPlayerState(sender);
        if (player == null) {
            throw new IllegalArgumentException("Unknown player " + sender);
        }
        UUID secret = player.getSecret();
        return new C2SPacket(player, Packet.decrypt(data, buffer.position(), buffer.remaining(), secret));
    }

}
