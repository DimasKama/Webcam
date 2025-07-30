package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;
import ru.dimaskama.webcam.net.NalUnit;

public record VideoC2SPacket(
        NalUnit nal
) implements Packet {

    public VideoC2SPacket(ByteBuf buf) {
        this(new NalUnit(buf));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        nal.writeBytes(buf);
    }

    @Override
    public PacketType<VideoC2SPacket> getType() {
        return PacketType.VIDEO_C2S;
    }

    @Override
    public int getEstimatedSize() {
        return nal().getEstimatedSize();
    }

}
