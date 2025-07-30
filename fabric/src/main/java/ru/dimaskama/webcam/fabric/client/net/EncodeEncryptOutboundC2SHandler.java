package ru.dimaskama.webcam.fabric.client.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import ru.dimaskama.webcam.net.Encryption;
import ru.dimaskama.webcam.net.packet.Packet;

import javax.crypto.SecretKey;
import java.util.UUID;

public class EncodeEncryptOutboundC2SHandler extends ChannelOutboundHandlerAdapter {

    private final WebcamClient client;

    public EncodeEncryptOutboundC2SHandler(WebcamClient client) {
        this.client = client;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet packet) {
            ByteBuf encoded = ctx.alloc().buffer(packet.getEstimatedSizeWithId());
            byte[] encrypted;
            try {
                packet.encodeWithId(encoded);
                SecretKey secretKey = client.getSecretKey();
                if (encoded.hasArray()) {
                    encrypted = Encryption.encrypt(encoded.array(), encoded.arrayOffset() + encoded.readerIndex(), encoded.readableBytes(), secretKey);
                } else {
                    byte[] array = new byte[encoded.readableBytes()];
                    encoded.readBytes(array);
                    encrypted = Encryption.encrypt(array, 0, array.length, secretKey);
                }
            } catch (Exception e) {
                encoded.release();
                throw e;
            }
            UUID uuid = client.getPlayerUuid();
            ByteBuf buf = ctx.alloc().buffer(17 + encrypted.length);
            try {
                buf.writeByte(Packet.C2S_MAGIC)
                        .writeLong(uuid.getMostSignificantBits())
                        .writeLong(uuid.getLeastSignificantBits())
                        .writeBytes(encrypted);
                ctx.write(new DatagramPacket(buf, client.getServerAddress()));
            } catch (Exception e) {
                buf.release();
                throw e;
            }
        } else {
            ctx.write(msg);
        }
    }

}
