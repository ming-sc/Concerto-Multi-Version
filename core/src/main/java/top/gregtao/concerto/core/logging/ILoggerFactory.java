package top.gregtao.concerto.core.logging;

public interface ILoggerFactory {

    ILogger getLogger(String name);

    ILogger getLogger(Class<?> clazz);

}
