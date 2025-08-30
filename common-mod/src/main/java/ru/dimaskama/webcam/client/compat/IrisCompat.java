package ru.dimaskama.webcam.client.compat;

import ru.dimaskama.webcam.WebcamMod;
import ru.dimaskama.webcam.client.render.WebcamRenderTypes;

public class IrisCompat {

    public static boolean IS_IRIS_LOADED = WebcamMod.getService().isModLoaded("iris");

    public static void init() {
        if (IS_IRIS_LOADED) {
            net.irisshaders.iris.api.v0.IrisApi.getInstance().assignPipeline(WebcamRenderTypes.SQUARE_PIPELINE, net.irisshaders.iris.api.v0.IrisProgram.TEXTURED);
            net.irisshaders.iris.api.v0.IrisApi.getInstance().assignPipeline(WebcamRenderTypes.ROUND_PIPELINE, net.irisshaders.iris.api.v0.IrisProgram.TEXTURED);
        }
    }

}
