package top.gregtao.concerto.core;

import top.gregtao.concerto.core.logging.ILogger;
import top.gregtao.concerto.core.bridge.ComponentImpl;
import top.gregtao.concerto.core.bridge.IComponent;
import top.gregtao.concerto.core.config.ConfigFile;
import top.gregtao.concerto.core.logging.EmptyLogger;
import top.gregtao.concerto.core.logging.ILoggerFactory;
import top.gregtao.concerto.core.logging.LoggerFactory;

public class ConcertoCore {
    public static ILogger CLIENT_LOGGER = new EmptyLogger();

    public static ILogger SERVER_LOGGER = new EmptyLogger();

    public static final ConfigFile MUSIC_CONFIG = new ConfigFile("Concerto/musics.json");

    public static void init(IComponent component, ILoggerFactory loggerFactory) {
        ComponentImpl.init(component);
        LoggerFactory.setLoggerFactory(loggerFactory);
        CLIENT_LOGGER = LoggerFactory.getLogger("ConcertoCore Client");
        SERVER_LOGGER = LoggerFactory.getLogger("ConcertoCore Server");
    }
}
