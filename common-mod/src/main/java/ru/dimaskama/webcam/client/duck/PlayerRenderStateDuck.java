package ru.dimaskama.webcam.client.duck;

import ru.dimaskama.webcam.client.DisplayingVideo;

import javax.annotation.Nullable;

public interface PlayerRenderStateDuck {

    @Nullable
    DisplayingVideo webcam_getDisplayingVideo();

    void webcam_setDisplayingVideo(@Nullable DisplayingVideo displayingVideo);

}
