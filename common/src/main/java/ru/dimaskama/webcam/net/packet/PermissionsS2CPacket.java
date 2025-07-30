package ru.dimaskama.webcam.net.packet;

import io.netty.buffer.ByteBuf;

public record PermissionsS2CPacket(
        boolean broadcast,
        boolean view
) implements Packet {

    private static final int BROADCAST_FLAG = 0b1;
    private static final int VIEW_FLAG = 0b10;

    public static PermissionsS2CPacket decode(ByteBuf buf) {
        int flags = buf.readByte() & 0xFF;
        return new PermissionsS2CPacket((flags & BROADCAST_FLAG) != 0, (flags & VIEW_FLAG) != 0);
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        int flags = (broadcast ? BROADCAST_FLAG : 0) | (view ? VIEW_FLAG : 0);
        buf.writeByte(flags);
    }

    @Override
    public PacketType<PermissionsS2CPacket> getType() {
        return PacketType.PERMISSIONS_S2C;
    }

    @Override
    public int getEstimatedSize() {
        return 1;
    }

}
