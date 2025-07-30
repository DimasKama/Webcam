package ru.dimaskama.webcam.server;

import ru.dimaskama.webcam.net.Encryption;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerState {

    private final UUID uuid;
    private final String name;
    private final Set<UUID> blockedSources = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean broadcastPermission = new AtomicBoolean(true);
    private final AtomicBoolean viewPermission = new AtomicBoolean(true);
    @Nullable
    private volatile InetSocketAddress socketAddress;
    @Nullable
    private volatile UUID secret;
    @Nullable
    private SecretKey secretKey;
    private volatile boolean authenticated;
    private volatile long lastKeepAlive;
    private volatile boolean showWebcams = true;

    public PlayerState(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setSocketAddress(@Nullable InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Nullable
    public InetSocketAddress getSocketAddress() {
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

    public SecretKey getSecretAsKey() {
        if (secretKey == null) {
            secretKey = Encryption.uuidToKey(getSecret());
        }
        return secretKey;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (authenticated) {
            lastKeepAlive = System.currentTimeMillis();
        } else {
            blockedSources.clear();
            showWebcams = true;
        }
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

    public void addBlockedSource(UUID uuid) {
        blockedSources.add(uuid);
    }

    public void removeBlockedSource(UUID uuid) {
        blockedSources.remove(uuid);
    }

    public boolean isSourceBlocked(UUID uuid) {
        return blockedSources.contains(uuid);
    }

    public void setShowWebcams(boolean showWebcams) {
        this.showWebcams = showWebcams;
    }

    public boolean canShowWebcams() {
        return showWebcams;
    }

    public boolean updateBroadcastPermission(boolean value) {
        return broadcastPermission.getAndSet(value);
    }

    public boolean hasBroadcastPermission() {
        return broadcastPermission.get();
    }

    public boolean updateViewPermission(boolean value) {
        return viewPermission.getAndSet(value);
    }

    public boolean hasViewPermission() {
        return viewPermission.get();
    }

}
