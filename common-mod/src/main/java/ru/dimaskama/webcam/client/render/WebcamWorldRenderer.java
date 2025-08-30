package ru.dimaskama.webcam.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2fc;
import org.joml.Vector3dc;
import ru.dimaskama.webcam.client.DisplayingVideo;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.client.net.WebcamClient;
import ru.dimaskama.webcam.net.VideoSource;

public class WebcamWorldRenderer {

    public static void renderWorldWebcams(Camera camera, PoseStack poseStack, MultiBufferSource consumers) {
        WebcamClient client = WebcamClient.getInstance();
        if (client != null && client.hasViewPermission() && WebcamModClient.CONFIG.getData().showWebcams()) {
            client.getDisplayingVideos().values().forEach(displayingVideo -> {
                DisplayingVideo.RenderData renderData = displayingVideo.getRenderData();
                if (renderData != null && renderData.source() instanceof VideoSource.Custom custom) {
                    renderImage(camera, poseStack, consumers, custom, renderData.textureId());
                }
            });
        }
    }

    public static void renderImage(Camera camera, PoseStack poseStack, MultiBufferSource consumers, VideoSource.Custom custom, ResourceLocation textureId) {
        Vector3dc pos = custom.getPos();
        Entity cameraEntity = camera.getEntity();
        double maxDistance = custom.getMaxDistance();
        if (cameraEntity.position().distanceToSqr(pos.x(), pos.y(), pos.z()) <= maxDistance * maxDistance) {
            poseStack.pushPose();
            Vec3 cameraPos = camera.getPosition();
            poseStack.translate(pos.x() - cameraPos.x, pos.y() - cameraPos.y, pos.z() - cameraPos.z);
            Vector2fc customRotation = custom.getCustomRotation();
            poseStack.mulPose(new Quaternionf().rotationYXZ(
                    (customRotation != null ? customRotation.y() - 90.0F : 180.0F - camera.getYRot()) * Mth.DEG_TO_RAD,
                    (customRotation != null ? customRotation.x() : -camera.getXRot()) * Mth.DEG_TO_RAD,
                    0.0F
            ));
            float halfWidth = 0.5F * custom.getWidth();
            float halfHeight = 0.5F * custom.getHeight();
            PoseStack.Pose pose = poseStack.last();
            WebcamRenderer.render(textureId, pose, consumers, halfWidth, halfHeight, custom.getShape());
            poseStack.popPose();
        }
    }

}
