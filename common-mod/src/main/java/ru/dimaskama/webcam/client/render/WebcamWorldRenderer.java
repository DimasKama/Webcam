package ru.dimaskama.webcam.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2fc;
import org.joml.Vector3dc;
import ru.dimaskama.webcam.client.DisplayingVideo;
import ru.dimaskama.webcam.client.DisplayingVideoManager;
import ru.dimaskama.webcam.client.WebcamModClient;
import ru.dimaskama.webcam.net.VideoSource;

public class WebcamWorldRenderer {

    public static void renderWorldWebcams(CameraRenderState camera, SubmitNodeStorage submitNodeStorage) {
        if (DisplayingVideoManager.INSTANCE.hasViewPermission() && WebcamModClient.CONFIG.getData().showWebcams()) {
            PoseStack poseStack = new PoseStack();
            DisplayingVideoManager.INSTANCE.forEach(displayingVideo -> {
                DisplayingVideo.RenderData renderData = displayingVideo.getRenderData();
                if (renderData != null && renderData.source() instanceof VideoSource.Custom custom) {
                    renderImage(camera, poseStack, submitNodeStorage, custom, renderData.textureId());
                }
            });
        }
    }

    public static void renderImage(CameraRenderState camera, PoseStack poseStack, SubmitNodeStorage submitNodeStorage, VideoSource.Custom custom, Identifier textureId) {
        Vector3dc pos = custom.getPos();
        double maxDistance = custom.getMaxDistance();
        if (camera.pos.distanceToSqr(pos.x(), pos.y(), pos.z()) <= maxDistance * maxDistance) {
            poseStack.pushPose();
            Vec3 cameraPos = camera.pos;
            poseStack.translate(pos.x() - cameraPos.x, pos.y() - cameraPos.y, pos.z() - cameraPos.z);
            Vector2fc customRotation = custom.getCustomRotation();
            poseStack.mulPose(customRotation != null
                    ? new Quaternionf().rotationYXZ(customRotation.y() - 90.0F, customRotation.x(), 0.0F)
                    : camera.orientation.conjugate(new Quaternionf()));
            float halfWidth = 0.5F * custom.getWidth();
            float halfHeight = 0.5F * custom.getHeight();
            WebcamRenderer.render(textureId, poseStack, submitNodeStorage, halfWidth, halfHeight, custom.getShape());
            poseStack.popPose();
        }
    }

}
