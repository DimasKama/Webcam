package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import ru.dimaskama.webcam.fabric.WebcamFabric;

import java.util.function.Function;

public class WebcamRenderTypes {

    private static final ShaderProgram SQUARE_SHADER = new ShaderProgram(WebcamFabric.id("core/square"), DefaultVertexFormat.POSITION_TEX, ShaderDefines.EMPTY);
    private static final Function<ResourceLocation, RenderType> SQUARE = Util.memoize(textureId -> RenderType.create(
            "webcam_square",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, TriState.TRUE, false))
                    .setShaderState(new RenderStateShard.ShaderStateShard(SQUARE_SHADER))
                    .createCompositeState(false)
    ));
    private static final ShaderProgram ROUND_SHADER = new ShaderProgram(WebcamFabric.id("core/round"), DefaultVertexFormat.POSITION_TEX, ShaderDefines.EMPTY);
    private static final Function<ResourceLocation, RenderType> ROUND = Util.memoize(textureId -> RenderType.create(
            "webcam_round",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, TriState.TRUE, false))
                    .setShaderState(new RenderStateShard.ShaderStateShard(ROUND_SHADER))
                    .createCompositeState(false)
    ));

    public static void init() {
        CoreShaders.getProgramsToPreload().add(SQUARE_SHADER);
        CoreShaders.getProgramsToPreload().add(ROUND_SHADER);
    }

    public static RenderType square(ResourceLocation textureId) {
        return SQUARE.apply(textureId);
    }

    public static RenderType round(ResourceLocation textureId) {
        return ROUND.apply(textureId);
    }

}
