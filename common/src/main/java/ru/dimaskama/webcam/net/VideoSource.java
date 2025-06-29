package ru.dimaskama.webcam.net;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import ru.dimaskama.webcam.config.VideoDisplayShape;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class VideoSource {

    private final UUID uuid;
    private final double maxDistance;

    protected VideoSource(UUID uuid, double maxDistance) {
        this.uuid = uuid;
        this.maxDistance = maxDistance;
    }

    protected VideoSource(ByteBuffer buffer) {
        this(new UUID(buffer.getLong(), buffer.getLong()), buffer.getDouble());
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public static VideoSource fromBytes(ByteBuffer buffer) {
        byte code = buffer.get();
        if (code == Face.CODE) {
            return new Face(buffer);
        }
        if (code == AboveHead.CODE) {
            return new AboveHead(buffer);
        }
        if (code == Custom.CODE) {
            return new Custom(buffer);
        }
        throw new IllegalArgumentException("Unknown VideoSource code: " + code);
    }

    public void writeBytes(ByteBuffer buffer) {
        buffer.put(getCode());
        buffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        buffer.putDouble(maxDistance);
    }

    protected abstract byte getCode();

    @Nullable
    protected static Vector2fc readCustomRotation(ByteBuffer buffer) {
        return buffer.get() != 0 ? new Vector2f(buffer.getFloat(), buffer.getFloat()) : null;
    }

    protected static void writeCustomRotation(ByteBuffer buffer, @Nullable Vector2fc customRotation) {
        if (customRotation != null) {
            buffer.put((byte) 1)
                    .putFloat(customRotation.x())
                    .putFloat(customRotation.y());
        } else {
            buffer.put((byte) 0);
        }
    }

    public static class Face extends VideoSource {

        public static final byte CODE = 0;

        public Face(UUID uuid, double maxDistance) {
            super(uuid, maxDistance);
        }

        public Face(ByteBuffer buffer) {
            super(buffer);
        }

        @Override
        public void writeBytes(ByteBuffer buffer) {
            super.writeBytes(buffer);
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

        public AboveHead(ByteBuffer buffer) {
            super(buffer);
            this.shape = VideoDisplayShape.byCode(buffer.get());
            this.offsetY = buffer.getFloat();
            this.size = buffer.getFloat();
            this.hideNickname = buffer.get() != 0;
            this.customRotation = readCustomRotation(buffer);
        }

        @Override
        public void writeBytes(ByteBuffer buffer) {
            super.writeBytes(buffer);
            buffer.put(shape.code)
                    .putFloat(offsetY)
                    .putFloat(size)
                    .put(hideNickname ? (byte) 1 : (byte) 0);
            writeCustomRotation(buffer, customRotation);
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
        private final float size;
        private final VideoDisplayShape shape;
        @Nullable
        private final Vector2fc customRotation;

        public Custom(UUID uuid, double maxDistance, Vector3dc pos, float size, VideoDisplayShape shape, @Nullable Vector2fc customRotation) {
            super(uuid, maxDistance);
            this.pos = pos;
            this.size = size;
            this.shape = shape;
            this.customRotation = customRotation;
        }

        public Custom(ByteBuffer buffer) {
            super(buffer);
            this.pos = new Vector3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
            this.size = buffer.getFloat();
            this.shape = VideoDisplayShape.byCode(buffer.get());
            this.customRotation = readCustomRotation(buffer);
        }

        @Override
        public void writeBytes(ByteBuffer buffer) {
            super.writeBytes(buffer);
            buffer.putDouble(pos.x()).putDouble(pos.y()).putDouble(pos.z())
                    .putFloat(size)
                    .put(shape.code);
            writeCustomRotation(buffer, customRotation);
        }

        @Override
        protected byte getCode() {
            return CODE;
        }

        public Vector3dc getPos() {
            return pos;
        }

        public float getSize() {
            return size;
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
