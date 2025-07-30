package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

public interface Packet {

    byte C2S_MAGIC = (byte) 0b11001100;
    byte S2C_MAGIC = (byte) 0b11101110;

    void writeBytes(ByteBuf buf);

    PacketType<?> getType();

    int getEstimatedSize();

    default int getEstimatedSizeWithId() {
        return 1 + getEstimatedSize();
    }

    default void encodeWithId(ByteBuf buf) {
        buf.writeByte(getType().getId());
        writeBytes(buf);
    }

    static Packet decodeById(ByteBuf buf) {
        return PacketType.byId(buf.readByte()).decode(buf);
    }

}
