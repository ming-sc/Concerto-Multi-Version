package top.gregtao.concerto.core.logging;

public interface ILogger {

    void debug(String msg);

    void debug(String msg, Throwable t);

    void debug(String format, Object... arguments);

    void info(String msg);

    void info(String msg, Throwable t);

    void info(String format, Object... arguments);

    void error(String msg);

    void error(String msg, Throwable t);

    void error(String format, Object... arguments);

    void warn(String msg);

    void warn(String msg, Throwable t);

    void warn(String format, Object... arguments);

}
