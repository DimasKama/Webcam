package ru.dimaskama.webcam.net;

import io.netty.buffer.ByteBuf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import ru.dimaskama.webcam.config.VideoDisplayShape;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class VideoSource {

    private final UUID uuid;
    private final double maxDistance;

    protected VideoSource(UUID uuid, double maxDistance) {
        this.uuid = uuid;
        this.maxDistance = maxDistance;
    }

    protected VideoSource(ByteBuf buf) {
        this(new UUID(buf.readLong(), buf.readLong()), buf.readDouble());
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public static VideoSource fromBytes(ByteBuf buf) {
        byte code = buf.readByte();
        if (code == Face.CODE) {
            return new Face(buf);
        }
        if (code == AboveHead.CODE) {
            return new AboveHead(buf);
        }
        if (code == Custom.CODE) {
            return new Custom(buf);
        }
        throw new IllegalArgumentException("Unknown VideoSource code: " + code);
    }

    public void writeBytes(ByteBuf buf) {
        buf.writeByte(getCode());
        buf.writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits());
        buf.writeDouble(maxDistance);
    }

    public abstract int getEstimatedSize();

    protected abstract byte getCode();

    @Nullable
    protected static Vector2fc readCustomRotation(ByteBuf buf) {
        return buf.readBoolean() ? new Vector2f(buf.readFloat(), buf.readFloat()) : null;
    }

    protected static void writeCustomRotation(ByteBuf buf, @Nullable Vector2fc customRotation) {
        if (customRotation != null) {
            buf.writeBoolean(true)
                    .writeFloat(customRotation.x())
                    .writeFloat(customRotation.y());
        } else {
            buf.writeBoolean(false);
        }
    }

    public static class Face extends VideoSource {

        public static final byte CODE = 0;

        public Face(UUID uuid, double maxDistance) {
            super(uuid, maxDistance);
        }

        public Face(ByteBuf buf) {
            super(buf);
        }

        @Override
        public void writeBytes(ByteBuf buf) {
            super.writeBytes(buf);
        }

        @Override
        public int getEstimatedSize() {
            return 25;
        }

        @Override
        protected byte getCode() {
            return CODE;
        }

    }

    public static class AboveHead extends VideoSource {

        public static final byte CODE = 1;
        private final VideoDisplayShape shape;
        private final float offsetY;
        private final float size;
        private final boolean hideNickname;
        @Nullable
        private final Vector2fc customRotation;

        public AboveHead(UUID uuid, double maxDistance, VideoDisplayShape shape, float offsetY, float size, boolean hideNickname, @Nullable Vector2fc customRotation) {
            super(uuid, maxDistance);
            this.shape = shape;
            this.offsetY = offsetY;
            this.size = size;
            this.hideNickname = hideNickname;
            this.customRotation = customRotation;
        }

        public AboveHead(ByteBuf buf) {
            super(buf);
            this.shape = VideoDisplayShape.byCode(buf.readByte());
            this.offsetY = buf.readFloat();
            this.size = buf.readFloat();
            this.hideNickname = buf.readBoolean();
            this.customRotation = readCustomRotation(buf);
        }

        @Override
        public void writeBytes(ByteBuf buf) {
            super.writeBytes(buf);
            buf.writeByte(shape.code)
                    .writeFloat(offsetY)
                    .writeFloat(size)
                    .writeBoolean(hideNickname);
            writeCustomRotation(buf, customRotation);
        }

        @Override
        public int getEstimatedSize() {
            return 36 + (customRotation != null ? 8 : 0);
        }

        @Override
        protected byte getCode() {
            return CODE;
        }

        public VideoDisplayShape getShape() {
            return shape;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public float getSize() {
            return size;
        }

        public boolean isHideNickname() {
            return hideNickname;
        }

        @Nullable
        public Vector2fc getCustomRotation() {
            return customRotation;
        }

    }

    public static class Custom extends VideoSource {

        public static final byte CODE = 2;
        private final Vector3dc pos;
        private final float width;
        private final float height;
        private final VideoDisplayShape shape;
        @Nullable
        private final Vector2fc customRotation;

        public Custom(UUID uuid, double maxDistance, Vector3dc pos, float width, float height, VideoDisplayShape shape, @Nullable Vector2fc customRotation) {
            super(uuid, maxDistance);
            this.pos = pos;
            this.width = width;
            this.height = height;
            this.shape = shape;
            this.customRotation = customRotation;
        }

        public Custom(ByteBuf buf) {
            super(buf);
            this.pos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            this.width = buf.readFloat();
            this.height = buf.readFloat();
            this.shape = VideoDisplayShape.byCode(buf.readByte());
            this.customRotation = readCustomRotation(buf);
        }

        @Override
        public void writeBytes(ByteBuf buf) {
            super.writeBytes(buf);
            buf.writeDouble(pos.x()).writeDouble(pos.y()).writeDouble(pos.z())
                    .writeFloat(width)
                    .writeFloat(height)
                    .writeByte(shape.code);
            writeCustomRotation(buf, customRotation);
        }

        @Override
        public int getEstimatedSize() {
            return 59 + (customRotation != null ? 8 : 0);
        }

        @Override
        protected byte getCode() {
            return CODE;
        }

        public Vector3dc getPos() {
            return pos;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public VideoDisplayShape getShape() {
            return shape;
        }

        @Nullable
        public Vector2fc getCustomRotation() {
            return customRotation;
        }

    }

}
