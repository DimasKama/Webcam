package ru.dimaskama.webcam.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.net.Encryption;
import ru.dimaskama.webcam.net.packet.Packet;

import java.util.UUID;

public class DecryptDecodeInboundC2SHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final WebcamServer server;

    public DecryptDecodeInboundC2SHandler(WebcamServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        if (buf.readByte() != Packet.C2S_MAGIC) {
            throw new IllegalArgumentException("Invalid C2S packet");
        }
        UUID sender = new UUID(buf.readLong(), buf.readLong());
        PlayerState player = server.getPlayerState(sender);
        if (player == null) {
            throw new IllegalArgumentException("Unknown player " + sender);
        }
        byte[] decrypted = Encryption.decrypt(buf, player.getSecretAsKey());
        Packet packet = Packet.decodeById(Unpooled.wrappedBuffer(decrypted));
        player.setSocketAddress(msg.sender());
        ctx.fireChannelRead(new C2SPacket(player, packet));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Webcam.isDebugMode()) {
            Webcam.getLogger().warn("Failed to decode C2S packet", cause);
        }
    }

}
