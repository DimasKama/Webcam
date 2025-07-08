package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2fc;
import ru.dimaskama.webcam.config.VideoDisplayShape;
import ru.dimaskama.webcam.fabric.client.DisplayingVideo;
import ru.dimaskama.webcam.fabric.client.WebcamClient;
import ru.dimaskama.webcam.net.VideoSource;

public class WebcamRenderLayer<M extends HumanoidModel<AbstractClientPlayer>> extends RenderLayer<AbstractClientPlayer, M> {

    private final EntityRenderDispatcher entityRenderDispatcher;

    public WebcamRenderLayer(RenderLayerParent<AbstractClientPlayer, M> renderLayerParent, EntityRenderDispatcher entityRenderDispatcher) {
        super(renderLayerParent);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, AbstractClientPlayer entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && !entity.isInvisibleTo(localPlayer)) {
            WebcamClient client = WebcamClient.getInstance();
            if (client != null) {
                DisplayingVideo displayingVideo = client.getDisplayingVideos().get(entity.getUUID());
                if (displayingVideo != null) {
                    DisplayingVideo.RenderData renderData = displayingVideo.getRenderData();
                    if (renderData != null) {
                        double maxDistance = renderData.source().getMaxDistance();
                        if (localPlayer.position().distanceToSqr(entity.position()) <= maxDistance * maxDistance) {
                            if (renderData.source() instanceof VideoSource.Face) {
                                poseStack.pushPose();
                                M entityModel = this.getParentModel();
                                entityModel.getHead().translateAndRotate(poseStack);
                                PoseStack.Pose pose = poseStack.last();
                                VertexConsumer consumer = consumers.getBuffer(WebcamRenderTypes.square(renderData.textureId()));
                                consumer.addVertex(pose, -0.25F, -0.5F, -0.26F).setUv(0.0F, 0.0F);
                                consumer.addVertex(pose, -0.25F, 0.0F, -0.26F).setUv(0.0F, 1.0F);
                                consumer.addVertex(pose, 0.25F, 0.0F, -0.26F).setUv(1.0F, 1.0F);
                                consumer.addVertex(pose, 0.25F, -0.5F, -0.26F).setUv(1.0F, 0.0F);
                                poseStack.popPose();
                            } else if (renderData.source() instanceof VideoSource.AboveHead aboveHead) {
                                poseStack.pushPose();
                                poseStack.translate(0.0F, -aboveHead.getOffsetY(), 0.0F);
                                Vector2fc rot = aboveHead.getCustomRotation();
                                poseStack.mulPose(new Quaternionf().rotationYXZ(
                                        ((rot != null ? rot.y() : 180.0F + entityRenderDispatcher.camera.getYRot()) - Mth.rotLerp(tickDelta, entity.yBodyRotO, entity.yBodyRot)) * Mth.DEG_TO_RAD,
                                        (rot != null ? rot.x() : -entityRenderDispatcher.camera.getXRot()) * Mth.DEG_TO_RAD,
                                        0.0F
                                ));
                                float sc = 0.5F * aboveHead.getSize();
                                PoseStack.Pose pose = poseStack.last();
                                VertexConsumer consumer = consumers.getBuffer(aboveHead.getShape() == VideoDisplayShape.ROUND
                                        ? WebcamRenderTypes.round(renderData.textureId())
                                        : WebcamRenderTypes.square(renderData.textureId()));
                                consumer.addVertex(pose, -sc, -sc, 0.0F).setUv(0.0F, 0.0F);
                                consumer.addVertex(pose, -sc, sc, 0.0F).setUv(0.0F, 1.0F);
                                consumer.addVertex(pose, sc, sc, 0.0F).setUv(1.0F, 1.0F);
                                consumer.addVertex(pose, sc, -sc, 0.0F).setUv(1.0F, 0.0F);
                                poseStack.popPose();
                            }
                        }
                    }
                }
            }
        }
    }

}
