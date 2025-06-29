package ru.dimaskama.webcam.logger;

public interface AbstractLogger {

    void info(String message);

    void info(String message, Throwable e);

    void warn(String message);

    void warn(String message, Throwable e);

    void error(String message);

    void error(String message, Throwable e);

}
