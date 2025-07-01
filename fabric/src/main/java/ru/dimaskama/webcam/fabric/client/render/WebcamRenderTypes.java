package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;

import java.util.function.Function;

public class WebcamRenderTypes {

    private static final RenderPipeline SQUARE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(WebcamFabric.id("pipeline/square"))
            .withVertexShader("core/position_tex")
            .withFragmentShader(WebcamFabric.id("core/square"))
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .build();
    private static final Function<ResourceLocation, RenderType> SQUARE = Util.memoize(textureId -> RenderType.create(
            "webcam_square",
            1536,
            SQUARE_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, false))
                    .createCompositeState(false)
    ));
    private static final RenderPipeline ROUND_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(WebcamFabric.id("pipeline/square"))
            .withVertexShader("core/position_tex")
            .withFragmentShader(WebcamFabric.id("core/round"))
            .withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .build();
    private static final Function<ResourceLocation, RenderType> ROUND = Util.memoize(textureId -> RenderType.create(
            "webcam_round",
            1536,
            ROUND_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, false))
                    .createCompositeState(false)
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
