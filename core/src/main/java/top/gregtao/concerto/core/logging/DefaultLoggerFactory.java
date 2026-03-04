package top.gregtao.concerto.core.logging;

public class DefaultLoggerFactory implements ILoggerFactory {

    @Override
    public ILogger getLogger(String name) {
        return new EmptyLogger();
    }

    @Override
    public ILogger getLogger(Class<?> clazz) {
        return new EmptyLogger();
    }

}
