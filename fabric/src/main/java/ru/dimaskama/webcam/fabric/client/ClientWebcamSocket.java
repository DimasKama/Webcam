package ru.dimaskama.webcam.fabric.client;

import ru.dimaskama.webcam.net.AbstractWebcamSocket;

import java.net.DatagramSocket;

public class ClientWebcamSocket extends AbstractWebcamSocket {

    public ClientWebcamSocket() throws Exception {
        socket = new DatagramSocket();
    }

}
