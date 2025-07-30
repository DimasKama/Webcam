package ru.dimaskama.webcam.net;

import io.netty.buffer.ByteBuf;

public record NalUnit(
        int sequenceNumber,
        byte[] data
) {

    public NalUnit(ByteBuf buf) {
        this(buf.readInt(), readData(buf));
    }

    public void writeBytes(ByteBuf buf) {
        buf.writeInt(sequenceNumber);
        buf.writeShort((short) data.length).writeBytes(data);
    }

    public int getEstimatedSize() {
        return 6 + data.length;
    }

    private static byte[] readData(ByteBuf buf) {
        int size = buf.readShort() & 0xFFFF;
        byte[] array = new byte[size];
        buf.readBytes(array);
        return array;
    }

}
