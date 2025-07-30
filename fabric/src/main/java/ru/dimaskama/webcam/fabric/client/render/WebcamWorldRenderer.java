package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2fc;
import org.joml.Vector3dc;
import ru.dimaskama.webcam.config.VideoDisplayShape;
import ru.dimaskama.webcam.fabric.client.DisplayingVideo;
import ru.dimaskama.webcam.fabric.client.WebcamFabricClient;
import ru.dimaskama.webcam.fabric.client.net.WebcamClient;
import ru.dimaskama.webcam.net.VideoSource;

public class WebcamWorldRenderer {

    public static void renderWorldWebcams(WorldRenderContext context) {
        WebcamClient client = WebcamClient.getInstance();
        if (client != null && client.hasViewPermission() && WebcamFabricClient.CONFIG.getData().showWebcams()) {
            client.getDisplayingVideos().values().forEach(displayingVideo -> {
                DisplayingVideo.RenderData renderData = displayingVideo.getRenderData();
                if (renderData != null && renderData.source() instanceof VideoSource.Custom custom) {
                    renderImage(context, custom, renderData.textureId());
                }
            });
        }
    }

    public static void renderImage(WorldRenderContext context, VideoSource.Custom custom, ResourceLocation textureId) {
        Vector3dc pos = custom.getPos();
        Entity cameraEntity = context.camera().getEntity();
        double maxDistance = custom.getMaxDistance();
        if (cameraEntity.position().distanceToSqr(pos.x(), pos.y(), pos.z()) <= maxDistance * maxDistance) {
            PoseStack poseStack = context.matrixStack();
            poseStack.pushPose();
            Camera camera = context.camera();
            Vec3 cameraPos = context.camera().getPosition();
            poseStack.translate(pos.x() - cameraPos.x, pos.y() - cameraPos.y, pos.z() - cameraPos.z);
            Vector2fc customRotation = custom.getCustomRotation();
            poseStack.mulPose(new Quaternionf().rotationYXZ(
                    (customRotation != null ? customRotation.y() - 90.0F : 180.0F - camera.getYRot()) * Mth.DEG_TO_RAD,
                    (customRotation != null ? customRotation.x() : -camera.getXRot()) * Mth.DEG_TO_RAD,
                    0.0F
            ));
            float w = 0.5F * custom.getWidth();
            float h = 0.5F * custom.getHeight();
            PoseStack.Pose pose = poseStack.last();
            VertexConsumer consumer = context.consumers().getBuffer(custom.getShape() == VideoDisplayShape.ROUND
                    ? WebcamRenderTypes.round(textureId)
                    : WebcamRenderTypes.square(textureId));
            consumer.addVertex(pose, -w, h, 0.0F).setUv(0.0F, 0.0F);
            consumer.addVertex(pose, -w, -h, 0.0F).setUv(0.0F, 1.0F);
            consumer.addVertex(pose, w, -h, 0.0F).setUv(1.0F, 1.0F);
            consumer.addVertex(pose, w, h, 0.0F).setUv(1.0F, 0.0F);
            poseStack.popPose();
        }
    }

}
