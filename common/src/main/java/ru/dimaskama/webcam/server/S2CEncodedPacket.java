package ru.dimaskama.webcam.server;

import io.netty.buffer.ByteBuf;

public record S2CEncodedPacket(
        PlayerState target,
        ByteBuf buf
) {

}
