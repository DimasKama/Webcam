package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class WebcamRenderTypes {

    private static final Function<ResourceLocation, RenderType> SQUARE = Util.memoize(textureId -> RenderType.create(
            "webcam_square",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, true, false))
                    .setShaderState(RenderType.POSITION_TEX_SHADER)
                    .createCompositeState(false)
    ));
    private static final Function<ResourceLocation, RenderType> ROUND = Util.memoize(textureId -> RenderType.create(
            "webcam_round",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.TRIANGLE_FAN,
            1536,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, true, false))
                    .setShaderState(RenderType.POSITION_TEX_SHADER)
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
