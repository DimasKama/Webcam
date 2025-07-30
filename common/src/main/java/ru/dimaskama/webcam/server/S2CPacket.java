package ru.dimaskama.webcam.server;

import ru.dimaskama.webcam.net.packet.Packet;

public record S2CPacket(
        PlayerState target,
        Packet packet
) {

}
