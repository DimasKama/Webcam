package ru.dimaskama.webcam.net;

import ru.dimaskama.webcam.Webcam;

import java.net.*;

public class ServerWebcamSocket extends AbstractWebcamSocket {

    private final int port;
    private final String bindAddress;

    public ServerWebcamSocket(int port, String bindAddress) throws Exception {
        this.port = port;
        this.bindAddress = bindAddress;
        InetAddress address = null;
        try {
            if (!bindAddress.isEmpty()) {
                address = InetAddress.getByName(bindAddress);
            }
        } catch (Exception e) {
            Webcam.getLogger().error("Failed to parse bind address \"" + bindAddress + "\"", e);
            bindAddress = null;
        }
        try {
            socket = new DatagramSocket(port, address);
        } catch (BindException e) {
            if (address == null || bindAddress.equals("0.0.0.0")) {
                throw e;
            }
            Webcam.getLogger().error("Failed to bind to address \"" + bindAddress + "\", binding to wildcard IP instead");
            socket = new DatagramSocket(port);
        }
    }

    public int getPort() {
        return port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

}
