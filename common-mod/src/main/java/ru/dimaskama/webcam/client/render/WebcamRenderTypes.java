package ru.dimaskama.webcam.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.WebcamModClient;

import java.util.function.Function;

public class WebcamRenderTypes {

    public static final RenderPipeline SQUARE_PIPELINE = RenderPipeline.builder()
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("ColorModulator", UniformType.VEC4)
            .withLocation(WebcamMod.id("pipeline/square"))
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .build();
    private static final Function<ResourceLocation, RenderType> SQUARE = Util.memoize(textureId -> WebcamModClient.getService().createWebcamRenderType(
            "webcam_square",
            SQUARE_PIPELINE,
            textureId
    ));
    public static final RenderPipeline ROUND_PIPELINE = RenderPipeline.builder()
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("ColorModulator", UniformType.VEC4)
            .withLocation(WebcamMod.id("pipeline/round"))
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLE_FAN)
            .build();
    private static final Function<ResourceLocation, RenderType> ROUND = Util.memoize(textureId -> WebcamModClient.getService().createWebcamRenderType(
            "webcam_round",
            ROUND_PIPELINE,
            textureId
    ));

    public static void init() {
    }

    public static RenderType square(ResourceLocation textureId) {
        return SQUARE.apply(textureId);
    }

    public static RenderType round(ResourceLocation textureId) {
        return ROUND.apply(textureId);
    }

}
