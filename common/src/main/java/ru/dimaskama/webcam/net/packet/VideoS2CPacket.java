package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;
import ru.dimaskama.webcam.net.NalUnit;
import ru.dimaskama.webcam.net.VideoSource;

public record VideoS2CPacket(
        VideoSource source,
        NalUnit nal
) implements Packet {

    public VideoS2CPacket(ByteBuf buf) {
        this(VideoSource.fromBytes(buf), new NalUnit(buf));
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        source.writeBytes(buf);
        nal.writeBytes(buf);
    }

    @Override
    public PacketType<VideoS2CPacket> getType() {
        return PacketType.VIDEO_S2C;
    }

    @Override
    public int getEstimatedSize() {
        return source.getEstimatedSize() + nal.getEstimatedSize();
    }

}
