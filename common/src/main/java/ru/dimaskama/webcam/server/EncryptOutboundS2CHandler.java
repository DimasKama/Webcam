package ru.dimaskama.webcam.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import ru.dimaskama.webcam.net.Encryption;
import ru.dimaskama.webcam.net.packet.Packet;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.util.UUID;

public class EncryptOutboundS2CHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof S2CEncodedPacket(PlayerState target, ByteBuf encoded)) {
            InetSocketAddress address = target.getSocketAddress();
            if (address == null) {
                throw new IllegalStateException("Player has not sent packets yet");
            }
            SecretKey secretKey = target.getSecretAsKey();
            byte[] encrypted;
            if (encoded.hasArray()) {
                encrypted = Encryption.encrypt(encoded.array(), encoded.arrayOffset() + encoded.readerIndex(), encoded.readableBytes(), secretKey);
            } else {
                byte[] array = new byte[encoded.readableBytes()];
                encoded.readBytes(array);
                encrypted = Encryption.encrypt(array, 0, array.length, secretKey);
            }
            UUID uuid = target.getUuid();
            ByteBuf buf = ctx.alloc().buffer(17 + encrypted.length);
            try {
                buf.writeByte(Packet.S2C_MAGIC)
                        .writeLong(uuid.getMostSignificantBits())
                        .writeLong(uuid.getLeastSignificantBits())
                        .writeBytes(encrypted);
                ctx.write(new DatagramPacket(buf, address));
            } catch (Exception e) {
                buf.release();
                throw e;
            }
        } else {
            ctx.write(msg);
        }
    }

}
