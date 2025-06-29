package ru.dimaskama.webcam.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dimaskama.webcam.Webcam;
import ru.dimaskama.webcam.logger.AbstractLogger;

public class WebcamPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        Logger logger = LoggerFactory.getLogger("Webcam");
        Webcam.initLogger(new AbstractLogger() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void info(String message, Throwable e) {
                logger.info(message, e);
            }

            @Override
            public void warn(String message) {
                logger.warn(message);
            }

            @Override
            public void warn(String message, Throwable e) {
                logger.warn(message, e);
            }

            @Override
            public void error(String message) {
                logger.error(message);
            }

            @Override
            public void error(String message, Throwable e) {
                logger.error(message, e);
            }
        });
    }

}
