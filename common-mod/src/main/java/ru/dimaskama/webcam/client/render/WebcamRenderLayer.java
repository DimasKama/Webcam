package ru.dimaskama.webcam.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2fc;
import ru.dimaskama.webcam.client.DisplayingVideo;
import ru.dimaskama.webcam.client.duck.PlayerRenderStateDuck;
import ru.dimaskama.webcam.net.VideoSource;

public class WebcamRenderLayer<M extends HumanoidModel<PlayerRenderState>> extends RenderLayer<PlayerRenderState, M> {

    private final EntityRenderDispatcher entityRenderDispatcher;

    public WebcamRenderLayer(RenderLayerParent<PlayerRenderState, M> renderLayerParent, EntityRenderDispatcher entityRenderDispatcher) {
        super(renderLayerParent);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, PlayerRenderState renderState, float limbAngle, float limbDistance) {
        if (!renderState.isInvisible) {
            DisplayingVideo displayingVideo = ((PlayerRenderStateDuck) renderState).webcam_getDisplayingVideo();
            if (displayingVideo != null) {
                DisplayingVideo.RenderData renderData = displayingVideo.getRenderData();
                if (renderData != null) {
                    LocalPlayer localPlayer = Minecraft.getInstance().player;
                    double maxDistance = renderData.source().getMaxDistance();
                    if (localPlayer == null || localPlayer.position().distanceToSqr(renderState.x, renderState.y, renderState.z) <= maxDistance * maxDistance) {
                        if (renderData.source() instanceof VideoSource.Face) {
                            poseStack.pushPose();
                            M entityModel = this.getParentModel();
                            entityModel.root().translateAndRotate(poseStack);
                            entityModel.getHead().translateAndRotate(poseStack);
                            PoseStack.Pose pose = poseStack.last();
                            poseStack.translate(0.0F, -0.25F, -0.26F);
                            WebcamRenderer.renderSquare(renderData.textureId(), pose, consumers, 0.25F, 0.25F);
                            poseStack.popPose();
                        } else if (renderData.source() instanceof VideoSource.AboveHead aboveHead) {
                            poseStack.pushPose();
                            poseStack.translate(0.0F, -aboveHead.getOffsetY(), 0.0F);
                            Vector2fc rot = aboveHead.getCustomRotation();
                            poseStack.mulPose(new Quaternionf().rotationYXZ(
                                    ((rot != null ? rot.y() : 180.0F + entityRenderDispatcher.camera.getYRot()) - renderState.bodyRot) * Mth.DEG_TO_RAD,
                                    (rot != null ? rot.x() : -entityRenderDispatcher.camera.getXRot()) * Mth.DEG_TO_RAD,
                                    0.0F
                            ));
                            float halfSize = 0.5F * aboveHead.getSize();
                            PoseStack.Pose pose = poseStack.last();
                            WebcamRenderer.render(renderData.textureId(), pose, consumers, halfSize, halfSize, aboveHead.getShape());
                            poseStack.popPose();
                        }
                    }
                }
            }
        }
    }

}
