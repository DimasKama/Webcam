package ru.dimaskama.webcam.fabric.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

import java.util.function.Consumer;

public class VideoPacketBuffer {

    private final Int2ObjectSortedMap<byte[]> buffer;
    private final int maxBufferSize;
    private final Consumer<byte[]> packetConsumer;
    private int expectedSequence = -1;

    public VideoPacketBuffer(int maxBufferSize, Consumer<byte[]> packetConsumer) {
        buffer = new Int2ObjectAVLTreeMap<>();
        this.maxBufferSize = maxBufferSize;
        this.packetConsumer = packetConsumer;
    }

    public void receivePacket(int sequenceNumber, byte[] packet) {
        if (expectedSequence == -1) {
            expectedSequence = sequenceNumber;
        } else if (sequenceNumber < expectedSequence) {
            return;
        }
        buffer.put(sequenceNumber, packet);
        while (buffer.containsKey(expectedSequence)) {
            packetConsumer.accept(buffer.remove(expectedSequence));
            ++expectedSequence;
        }
        if (buffer.size() > maxBufferSize) {
            for (byte[] p : buffer.values()) {
                packetConsumer.accept(p);
            }
            buffer.clear();
            expectedSequence = -1;
        }
    }

}
