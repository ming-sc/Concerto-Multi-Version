package top.gregtao.concerto.core.music.meta.music;

import top.gregtao.concerto.core.bridge.ComponentImpl;

public class UnknownMusicMeta extends TimelessMusicMetaData {

    public UnknownMusicMeta(String source) {
        super(ComponentImpl.getTranslatable("concerto.unknown"), ComponentImpl.getTranslatable("concerto.unknown"), source);
    }
}
