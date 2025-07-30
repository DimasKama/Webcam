package ru.dimaskama.webcam.velocity;


import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.UUID;

public final class ProxyBridge {

    private final UUID playerUuid;
    private final InetSocketAddress serverAddress;
    @Nullable
    private InetSocketAddress playerAddress;
    private boolean addressLocked;

    public ProxyBridge(UUID playerUuid, InetSocketAddress serverAddress) {
        this.playerUuid = playerUuid;
        this.serverAddress = serverAddress;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    @Nullable
    public InetSocketAddress getPlayerAddress() {
        return playerAddress;
    }

    public void updatePlayerAddress(InetSocketAddress playerAddress) {
        if (!addressLocked) {
            this.playerAddress = playerAddress;
        }
    }

    public void lockAddress() {
        addressLocked = true;
    }

}
