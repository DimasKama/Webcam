package ru.dimaskama.webcam.message;

import io.netty.buffer.ByteBuf;

public interface Message {

    void writeBytes(ByteBuf buf);

    Channel<?> getChannel();

}
