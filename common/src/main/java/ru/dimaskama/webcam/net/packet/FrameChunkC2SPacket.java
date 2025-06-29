package ru.dimaskama.webcam.net.packet;

import ru.dimaskama.webcam.net.FrameChunk;

import java.nio.ByteBuffer;

public record FrameChunkC2SPacket(
        FrameChunk chunk
) implements Packet {

    public FrameChunkC2SPacket(ByteBuffer buffer) {
        this(new FrameChunk(buffer));
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        chunk.writeBytes(buffer);
    }

    @Override
    public PacketType<FrameChunkC2SPacket> getType() {
        return PacketType.FRAME_CHUNK_C2S;
    }

}
