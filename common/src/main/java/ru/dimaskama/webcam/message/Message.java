package ru.dimaskama.webcam.message;

import java.nio.ByteBuffer;

public interface Message {

    void writeBytes(ByteBuffer buffer);

    Channel<?> getChannel();

}
