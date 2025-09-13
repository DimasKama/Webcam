package ru.dimaskama.webcam.client.fabric.compat.replay;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.net.KnownSource;
import ru.dimaskama.webcam.net.NalUnit;
import ru.dimaskama.webcam.net.VideoSource;

import java.util.List;
import java.util.UUID;

public interface ReplayWebcamMessage extends CustomPacketPayload {

    Type<ReplayWebcamMessage> TYPE = new Type<>(WebcamMod.id("replay"));
    StreamCodec<RegistryFriendlyByteBuf, ReplayWebcamMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeByte(msg.code());
                subStreamCodecByCode(msg.code()).encode(buf, msg);
            },
            buf -> {
                byte code = buf.readByte();
                return subStreamCodecByCode(code).decode(buf);
            }
    );

    @Override
    default Type<ReplayWebcamMessage> type() {
        return TYPE;
    }

    byte code();

    default boolean shouldAcceptInFastForwarding() {
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static StreamCodec<RegistryFriendlyByteBuf, ReplayWebcamMessage> subStreamCodecByCode(int code) {
        if (code == Video.CODE) {
            return (StreamCodec) Video.SUB_STREAM_CODEC;
        }
        if (code == CloseSource.CODE) {
            return (StreamCodec) CloseSource.SUB_STREAM_CODEC;
        }
        if (code == ViewPermission.CODE) {
            return (StreamCodec) ViewPermission.SUB_STREAM_CODEC;
        }
        if (code == KnownSources.CODE) {
            return (StreamCodec) KnownSources.SUB_STREAM_CODEC;
        }
        throw new IllegalArgumentException("Unknown ReplayWebcamMessage code: " + code);
    }

    record Video(
            VideoSource source,
            NalUnit nal
    ) implements ReplayWebcamMessage {

        public static final byte CODE = (byte) 0;
        private static final StreamCodec<RegistryFriendlyByteBuf, Video> SUB_STREAM_CODEC = StreamCodec.of(
                (buf, msg) -> {
                    msg.source().writeBytes(buf);
                    msg.nal().writeBytes(buf);
                },
                buf -> {
                    VideoSource source = VideoSource.fromBytes(buf);
                    NalUnit nal = new NalUnit(buf);
                    return new Video(source, nal);
                }
        );

        @Override
        public byte code() {
            return CODE;
        }

        @Override
        public boolean shouldAcceptInFastForwarding() {
            return false;
        }

    }

    record CloseSource(
            UUID sourceUuid
    ) implements ReplayWebcamMessage {

        public static final byte CODE = (byte) 1;
        public static final StreamCodec<RegistryFriendlyByteBuf, CloseSource> SUB_STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                CloseSource::sourceUuid,
                CloseSource::new
        );

        @Override
        public byte code() {
            return CODE;
        }

    }

    record ViewPermission(
            boolean view
    ) implements ReplayWebcamMessage {

        public static final byte CODE = (byte) 2;
        public static final StreamCodec<RegistryFriendlyByteBuf, ViewPermission> SUB_STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL ,
                ViewPermission::view,
                ViewPermission::new
        );

        @Override
        public byte code() {
            return CODE;
        }

    }

    record KnownSources(
            List<KnownSource> sources
    ) implements ReplayWebcamMessage {

        public static final byte CODE = (byte) 3;
        public static final StreamCodec<RegistryFriendlyByteBuf, KnownSources> SUB_STREAM_CODEC = StreamCodec.composite(
                StreamCodec.of(
                        (RegistryFriendlyByteBuf buf, KnownSource src) -> src.writeBytes(buf),
                        KnownSource::new
                ).apply(ByteBufCodecs.list()),
                KnownSources::sources,
                KnownSources::new
        );

        @Override
        public byte code() {
            return CODE;
        }

    }

}
