package top.gregtao.concerto.core.logging;

public class EmptyLogger implements ILogger {
    @Override
    public void debug(String msg) {
    }

    @Override
    public void debug(String msg, Throwable t) {
    }

    @Override
    public void debug(String format, Object... arguments) {
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void info(String msg, Throwable t) {
    }

    @Override
    public void info(String format, Object... arguments) {
    }

    @Override
    public void error(String msg) {
    }

    @Override
    public void error(String msg, Throwable t) {
    }

    @Override
    public void error(String format, Object... arguments) {
    }

    @Override
    public void warn(String msg) {
    }

    @Override
    public void warn(String msg, Throwable t) {
    }

    @Override
    public void warn(String format, Object... arguments) {
    }
}
