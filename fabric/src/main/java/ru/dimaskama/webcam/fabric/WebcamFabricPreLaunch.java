package ru.dimaskama.webcam.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.LoggerFactory;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.logger.Slf4jLogger;

public class WebcamFabricPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        Webcam.initLogger(new Slf4jLogger(LoggerFactory.getLogger("Webcam")));
    }

}
