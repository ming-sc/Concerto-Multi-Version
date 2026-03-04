package top.gregtao.concerto.core.logging;

import java.util.Objects;

public class LoggerFactory {

    private static ILoggerFactory LOGGER_FACTORY_IMPL = new DefaultLoggerFactory();

    public static void setLoggerFactory(ILoggerFactory loggerFactory) {
        Objects.requireNonNull(loggerFactory);
        LOGGER_FACTORY_IMPL = loggerFactory;
    }

    public static ILogger getLogger(String name) {
        return LOGGER_FACTORY_IMPL.getLogger(name);
    }

    public static ILogger getLogger(Class<?> clazz) {
        return LOGGER_FACTORY_IMPL.getLogger(clazz);
    }

}
