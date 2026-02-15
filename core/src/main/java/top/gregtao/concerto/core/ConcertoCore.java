package top.gregtao.concerto.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.gregtao.concerto.core.bridge.ComponentImpl;
import top.gregtao.concerto.core.bridge.IComponent;
import top.gregtao.concerto.core.config.ConfigFile;

public class ConcertoCore {
    public static Logger CLIENT_LOGGER = LoggerFactory.getLogger("ConcertoCoreClient");

    public static Logger SERVER_LOGGER = LoggerFactory.getLogger("ConcertoCoreServer");

    public static Logger LOGGER = LoggerFactory.getLogger("ConcertoCore");

    public static final ConfigFile MUSIC_CONFIG = new ConfigFile("Concerto/musics.json");

    public static void init(IComponent component) {
        ComponentImpl.init(component);
    }
}
