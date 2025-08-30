package ru.dimaskama.webcam.client.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.net.Encryption;
import ru.dimaskama.webcam.net.packet.Packet;

import java.util.UUID;

public class DecryptDecodeInboundS2CHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final WebcamClient client;

    public DecryptDecodeInboundS2CHandler(WebcamClient client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        if (buf.readByte() != Packet.S2C_MAGIC) {
            throw new IllegalArgumentException("Invalid S2C packet");
        }
        UUID target = new UUID(buf.readLong(), buf.readLong());
        if (!client.getPlayerUuid().equals(target)) {
            throw new IllegalArgumentException("Packet is targeted to a different player");
        }
        byte[] decrypted = Encryption.decrypt(buf, client.getSecretKey());
        Packet packet = Packet.decodeById(Unpooled.wrappedBuffer(decrypted));
        ctx.fireChannelRead(packet);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Webcam.isDebugMode()) {
            Webcam.getLogger().warn("Failed to decode S2C packet", cause);
        }
    }

}
