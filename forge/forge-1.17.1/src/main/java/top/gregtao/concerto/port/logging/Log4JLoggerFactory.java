package top.gregtao.concerto.port.logging;

import org.apache.logging.log4j.LogManager;
import top.gregtao.concerto.core.logging.ILogger;
import top.gregtao.concerto.core.logging.ILoggerFactory;

public class Log4JLoggerFactory implements ILoggerFactory {
    @Override
    public ILogger getLogger(String name) {
        return new LoggerAdapterLog4J(LogManager.getLogger(name));
    }

    @Override
    public ILogger getLogger(Class<?> clazz) {
        return new LoggerAdapterLog4J(LogManager.getLogger(clazz));
    }
}
