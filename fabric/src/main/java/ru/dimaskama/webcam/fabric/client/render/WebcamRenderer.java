package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import ru.dimaskama.webcam.config.VideoDisplayShape;

public class WebcamRenderer {

    public static void render(ResourceLocation textureId, PoseStack.Pose pose, MultiBufferSource consumers, float halfWidth, float halfHeight, VideoDisplayShape shape) {
        switch (shape) {
            case SQUARE -> renderSquare(textureId, pose, consumers, halfWidth, halfHeight);
            case ROUND -> renderRound(textureId, pose, consumers, halfWidth, halfHeight);
        }
    }

    public static void renderSquare(ResourceLocation textureId, PoseStack.Pose pose, MultiBufferSource consumers, float halfWidth, float halfHeight) {
        VertexConsumer consumer = consumers.getBuffer(WebcamRenderTypes.square(textureId));
        consumer.addVertex(pose, -halfWidth, -halfHeight, 0.0F).setUv(0.0F, 0.0F);
        consumer.addVertex(pose, -halfWidth, halfHeight, 0.0F).setUv(0.0F, 1.0F);
        consumer.addVertex(pose, halfWidth, halfHeight, 0.0F).setUv(1.0F, 1.0F);
        consumer.addVertex(pose, halfWidth, -halfHeight, 0.0F).setUv(1.0F, 0.0F);
    }

    public static void renderRound(ResourceLocation textureId, PoseStack.Pose pose, MultiBufferSource consumers, float halfWidth, float halfHeight) {
        VertexConsumer consumer = consumers.getBuffer(WebcamRenderTypes.round(textureId));
        int numVertices = Mth.clamp(Math.round(8.0F * Mth.TWO_PI * halfWidth), 48, 360);
        consumer.addVertex(pose, 0.0F, 0.0F, 0.0F).setUv(0.5F, 0.5F);
        for (int i = 0; i <= numVertices; i++) {
            float angle = Mth.TWO_PI * i / numVertices;
            float x = Mth.cos(angle);
            float y = -Mth.sin(angle);
            consumer.addVertex(pose, halfWidth * x, halfHeight * y, 0.0F).setUv(0.5F * (x + 1.0F), 0.5F * (y + 1.0F));
        }
    }

}
