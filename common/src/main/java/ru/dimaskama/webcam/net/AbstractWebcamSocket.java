package ru.dimaskama.webcam.net;

import javax.annotation.Nullable;
import java.net.*;

public abstract class AbstractWebcamSocket {

    protected final byte[] packetBuf = new byte[4096];
    @Nullable
    protected DatagramSocket socket;

    private DatagramSocket getSocket() {
        DatagramSocket socket = this.socket;
        if (socket == null) {
            throw new IllegalStateException("socket is closed");
        }
        return socket;
    }

    public RawPacket read() throws Exception {
        DatagramSocket socket = getSocket();
        DatagramPacket packet = new DatagramPacket(packetBuf, packetBuf.length);
        socket.receive(packet);
        long timestamp = System.currentTimeMillis();
        int len = packet.getLength();
        byte[] data = new byte[len];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, len);
        return new RawPacket(data, packet.getSocketAddress(), timestamp);
    }

    public void send(SocketAddress address, byte[] packet) throws Exception {
        DatagramSocket socket = getSocket();
        socket.send(new DatagramPacket(packet, packet.length, address));
    }

    public void close() {
        DatagramSocket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            socket.close();
        }
    }

    public boolean isClosed() {
        return socket == null;
    }

}
