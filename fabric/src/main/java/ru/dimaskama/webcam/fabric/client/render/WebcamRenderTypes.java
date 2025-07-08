package ru.dimaskama.webcam.fabric.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.fabric.WebcamFabric;

import java.util.function.Function;

public class WebcamRenderTypes {

    private static ShaderInstance squareShader;
    private static final Function<ResourceLocation, RenderType> SQUARE = Util.memoize(textureId -> RenderType.create(
            "webcam_square",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, true, false))
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> squareShader))
                    .createCompositeState(false)
    ));
    private static ShaderInstance roundShader;
    private static final Function<ResourceLocation, RenderType> ROUND = Util.memoize(textureId -> RenderType.create(
            "webcam_round",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(textureId, true, false))
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> roundShader))
                    .createCompositeState(false)
    ));

    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(WebcamFabric.id("square"), DefaultVertexFormat.POSITION_TEX, s -> squareShader = s);
            context.register(WebcamFabric.id("round"), DefaultVertexFormat.POSITION_TEX, s -> roundShader = s);
        });
    }

    public static RenderType square(ResourceLocation textureId) {
        return SQUARE.apply(textureId);
    }

    public static RenderType round(ResourceLocation textureId) {
        return ROUND.apply(textureId);
    }

}
