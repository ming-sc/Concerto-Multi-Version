package top.gregtao.concerto.port.logging;

import org.slf4j.LoggerFactory;
import top.gregtao.concerto.core.logging.ILogger;
import top.gregtao.concerto.core.logging.ILoggerFactory;

public class Slf4JLoggerFactory implements ILoggerFactory {

    @Override
    public ILogger getLogger(String name) {
        return new LoggerAdapterSlf4J(LoggerFactory.getLogger(name));
    }

    @Override
    public ILogger getLogger(Class<?> clazz) {
        return new LoggerAdapterSlf4J(LoggerFactory.getLogger(clazz));
    }

}
