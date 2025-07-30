package ru.dimaskama.webcam.server;

import ru.dimaskama.webcam.net.packet.Packet;

public record C2SPacket(
        PlayerState sender,
        Packet packet
) {

}
