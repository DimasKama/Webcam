package ru.dimaskama.webcam.fabric.client.duck;

import ru.dimaskama.webcam.fabric.client.DisplayingVideo;

import javax.annotation.Nullable;

public interface PlayerRenderStateDuck {

    @Nullable
    DisplayingVideo webcam_getDisplayingVideo();

    void webcam_setDisplayingVideo(@Nullable DisplayingVideo displayingVideo);

}
