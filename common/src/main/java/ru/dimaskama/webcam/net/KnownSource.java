package ru.dimaskama.webcam.net;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class KnownSource {

    public static final int ICON_DIMENSION = 16;
    public static final int ICON_SIZE = 16 * 16 * 4;
    private final UUID uuid;
    private final String name;
    @Nullable
    private final byte[] customIcon;
    private byte[] nameBytesCache;

    public KnownSource(UUID uuid, String name) {
        this(uuid, name, null);
    }

    public KnownSource(UUID uuid, String name, @Nullable byte[] customIcon) {
        this.uuid = uuid;
        this.name = name;
        if (customIcon != null && customIcon.length != ICON_SIZE) {
            throw new IllegalArgumentException("customIcon must be a 16x16 raw RGBA image with size " + ICON_SIZE);
        }
        this.customIcon = customIcon;
    }

    public KnownSource(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        int nameLength = buf.readByte();
        byte[] nameBytes = new byte[nameLength];
        buf.readBytes(nameBytes);
        name = new String(nameBytes, StandardCharsets.UTF_8);
        byte[] customIcon = null;
        if (buf.readBoolean()) {
            customIcon = new byte[ICON_SIZE];
            buf.readBytes(customIcon);
        }
        this.customIcon = customIcon;
    }

    public void writeBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits());
        byte[] nameBytes = getNameBytes();
        buf.writeByte(nameBytes.length).writeBytes(nameBytes);
        buf.writeBoolean(customIcon != null);
        if (customIcon != null) {
            buf.writeBytes(customIcon);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public byte[] getCustomIcon() {
        return customIcon;
    }

    private byte[] getNameBytes() {
        if (nameBytesCache == null) {
            nameBytesCache = name.getBytes(StandardCharsets.UTF_8);
        }
        return nameBytesCache;
    }

    public int getSize() {
        return 18 + getNameBytes().length + (customIcon != null ? ICON_SIZE : 0);
    }

}
