package ru.dimaskama.webcam.server;

public class KeepAliveThread extends Thread {

    private final WebcamServer server;

    public KeepAliveThread(WebcamServer server) {
        this.server = server;
        setName("KeepAliveThread");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            server.sendKeepAlives();
            try {
                sleep(server.getKeepAlivePeriod());
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }

}
