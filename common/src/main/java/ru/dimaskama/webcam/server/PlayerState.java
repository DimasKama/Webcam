package ru.dimaskama.webcam.server;

import ru.dimaskama.webcam.net.FrameChunk;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.security.SecureRandom;
import java.util.UUID;

public class PlayerState {

    private final UUID uuid;
    @Nullable
    private SocketAddress socketAddress;
    @Nullable
    private volatile UUID secret;
    private boolean authenticated;
    private long lastKeepAlive = System.currentTimeMillis();
    private long sequenceNumber = -1;
    private int frameNumber = -1;
    private FrameChunk.Type frameChunkType = FrameChunk.Type.SINGLE;

    public PlayerState(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setSocketAddress(@Nullable SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Nullable
    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public UUID getSecret() {
        if (secret == null) {
            synchronized (this) {
                if (secret == null) {
                    SecureRandom random = new SecureRandom();
                    secret = new UUID(random.nextLong(), random.nextLong());
                }
            }
        }
        return secret;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public void setLastKeepAlive(long lastKeepAliveResponse) {
        this.lastKeepAlive = lastKeepAliveResponse;
    }

    public boolean onFrameChunk(FrameChunk chunk) {
        boolean accept = frameNumber != chunk.frameNumber()
                || sequenceNumber + 1 == chunk.sequenceNumber() && frameChunkType.isNextExpected(chunk.type());
        sequenceNumber = chunk.sequenceNumber();
        frameNumber = chunk.frameNumber();
        frameChunkType = chunk.type();
        return accept;
    }

}
