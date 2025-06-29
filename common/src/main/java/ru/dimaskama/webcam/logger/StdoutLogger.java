package ru.dimaskama.webcam.logger;

public class StdoutLogger implements AbstractLogger {

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void info(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace(System.out);
    }

    @Override
    public void warn(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace(System.out);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

    @Override
    public void error(String message, Throwable e) {
        System.err.println(message);
        e.printStackTrace(System.err);
    }

}
