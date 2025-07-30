package ru.dimaskama.webcam.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import ru.dimaskama.webcam.net.packet.Packet;

public class EncodeOutboundS2CHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof S2CPacket(PlayerState target, Packet packet)) {
            ByteBuf buf = ctx.alloc().buffer(packet.getEstimatedSizeWithId());
            packet.encodeWithId(buf);
            ctx.write(new S2CEncodedPacket(target, buf));
        } else {
            ctx.write(msg);
        }
    }

}
