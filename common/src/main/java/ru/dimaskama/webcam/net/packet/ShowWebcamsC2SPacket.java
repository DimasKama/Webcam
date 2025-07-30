package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

public record ShowWebcamsC2SPacket(
        boolean showWebcams
) implements Packet {

    public ShowWebcamsC2SPacket(ByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        buf.writeBoolean(showWebcams);
    }

    @Override
    public PacketType<ShowWebcamsC2SPacket> getType() {
        return PacketType.SHOW_WEBCAMS_C2S;
    }

    @Override
    public int getEstimatedSize() {
        return 1;
    }

}
