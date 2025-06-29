package ru.dimaskama.webcam.net.packet;

import ru.dimaskama.webcam.net.FrameChunk;
import ru.dimaskama.webcam.net.VideoSource;

import java.nio.ByteBuffer;

public record FrameChunkS2CPacket(
        VideoSource source,
        FrameChunk chunk
) implements Packet {

    public FrameChunkS2CPacket(ByteBuffer buffer) {
        this(VideoSource.fromBytes(buffer), new FrameChunk(buffer));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        source.writeBytes(buffer);
        chunk.writeBytes(buffer);
    }

    @Override
    public PacketType<FrameChunkS2CPacket> getType() {
        return PacketType.FRAME_CHUNK_S2C;
    }

}
