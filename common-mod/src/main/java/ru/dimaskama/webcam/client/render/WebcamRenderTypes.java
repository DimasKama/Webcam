package ru.dimaskama.webcam.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.WebcamModClient;

import java.util.function.Function;

public class WebcamRenderTypes {

    public static final RenderPipeline SQUARE_PIPELINE = RenderPipeline.builder()
            .withLocation(WebcamMod.id("pipeline/square"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();
    private static final Function<Identifier, RenderType> SQUARE = Util.memoize(textureId -> WebcamModClient.getService().createWebcamRenderType(
            "webcam_square",
            SQUARE_PIPELINE,
            textureId
    ));
    public static final RenderPipeline ROUND_PIPELINE = RenderPipeline.builder()
            .withLocation(WebcamMod.id("pipeline/round"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();
    private static final Function<Identifier, RenderType> ROUND = Util.memoize(textureId -> WebcamModClient.getService().createWebcamRenderType(
            "webcam_round",
            ROUND_PIPELINE,
            textureId
    ));

    public static void init() {
    }

    public static RenderType square(Identifier textureId) {
        return SQUARE.apply(textureId);
    }

    public static RenderType round(Identifier textureId) {
        return ROUND.apply(textureId);
    }

}
