package ru.dimaskama.webcam.net.packet;

import ru.dimaskama.webcam.Utils;
import ru.dimaskama.webcam.net.Encryption;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface Packet {

    void writeBytes(ByteBuffer buffer);

    PacketType<?> getType();

    default byte[] encrypt(UUID secret) throws Exception {
        byte[] tempBuf = Utils.TEMP_BUFFERS.get();
        ByteBuffer buffer = ByteBuffer.wrap(tempBuf);
        buffer.put(getType().getId());
        writeBytes(buffer);
        return Encryption.encrypt(tempBuf, 0, buffer.position(), secret);
    }

    static Packet decrypt(byte[] data, int offset, int length, UUID secret) throws Exception {
        byte[] decrypted = Encryption.decrypt(data, offset, length, secret);
        if (decrypted.length == 0) {
            throw new IllegalArgumentException("Empty packet");
        }
        ByteBuffer buffer = ByteBuffer.wrap(decrypted);
        byte packetTypeId = buffer.get();
        PacketType<?> packetType = PacketType.byId(packetTypeId);
        return packetType.decode(buffer);
    }

}
