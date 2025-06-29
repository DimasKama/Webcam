package ru.dimaskama.webcam.net;

import java.net.SocketAddress;

public record RawPacket(
        byte[] data,
        SocketAddress address,
        long timestamp
) {

}
