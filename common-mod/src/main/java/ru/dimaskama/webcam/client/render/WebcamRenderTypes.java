package ru.dimaskama.webcam.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import ru.dimaskama.webcam.client.WebcamModClient;

import java.util.function.Function;

public class WebcamRenderTypes {

    private static final Function<ResourceLocation, RenderType> SQUARE = Util.memoize(textureId -> WebcamModClient.getService().createWebcamRenderType(
            "webcam_square",
            VertexFormat.Mode.QUADS,
            textureId
    ));
    private static final Function<ResourceLocation, RenderType> ROUND = Util.memoize(textureId -> WebcamModClient.getService().createWebcamRenderType(
            "webcam_round",
            VertexFormat.Mode.TRIANGLE_FAN,
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
