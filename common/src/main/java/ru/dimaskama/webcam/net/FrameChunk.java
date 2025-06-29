package ru.dimaskama.webcam.net;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record FrameChunk(long sequenceNumber, int frameNumber, Type type, byte[] chunk) {

    public FrameChunk(ByteBuffer buffer) {
        this(buffer.getLong(), buffer.getInt(), Type.byCode(buffer.get()), readPayload(buffer));
    }

    private static byte[] readPayload(ByteBuffer buffer) {
        int size = buffer.getShort();
        byte[] chunk = new byte[size];
        buffer.get(chunk);
        return chunk;
    }

    public void writeBytes(ByteBuffer buffer) {
        buffer.putLong(sequenceNumber)
                .putInt(frameNumber)
                .put(type.code)
                .putShort((short) chunk.length)
                .put(chunk);
    }

    public static List<FrameChunk> split(long sequenceNumber, int frameNumber, byte[] frame, int mtu) {
        int total = frame.length;
        List<FrameChunk> list = new ArrayList<>();
        for (int i = 0; i < total; i += mtu) {
            boolean isStart = i == 0;
            boolean isEnd = i + mtu >= total;
            Type type = isStart
                    ? isEnd ? Type.SINGLE : Type.START
                    : isEnd ? Type.END : Type.MID;
            if (type == Type.SINGLE) {
                // Do not create new arrays
                list.add(new FrameChunk(sequenceNumber, frameNumber, type, frame));
                break;
            }
            byte[] chunk = new byte[isEnd ? total - i : mtu];
            System.arraycopy(frame, i, chunk, 0, chunk.length);
            list.add(new FrameChunk(sequenceNumber++, frameNumber, type, chunk));
        }
        return list;
    }

    public enum Type {

        MID((byte) 0) {
            @Override
            public boolean isNextExpected(Type nextChunkType) {
                return nextChunkType == MID || nextChunkType == END;
            }
        },
        START((byte) 1) {
            @Override
            public boolean isNextExpected(Type nextChunkType) {
                return nextChunkType == MID || nextChunkType == END;
            }
        },
        END((byte) 2) {
            @Override
            public boolean isNextExpected(Type nextChunkType) {
                return nextChunkType == START || nextChunkType == SINGLE;
            }
        },
        SINGLE((byte) 3) {
            @Override
            public boolean isNextExpected(Type nextChunkType) {
                return nextChunkType == START;
            }
        };

        public final byte code;

        Type(byte code) {
            this.code = code;
        }

        public static Type byCode(byte code) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid code: " + Integer.toBinaryString(code));
        }

        public abstract boolean isNextExpected(Type nextChunkType);

    }

}
