package top.gregtao.concerto.screen.widget;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.music.meta.music.MusicMetaData;
import top.gregtao.concerto.core.util.Pair;

import java.util.UUID;

public class MusicWithUUIDListWidget extends ConcertoListWidget<Pair<Music, UUID>> {

    public MusicWithUUIDListWidget(int width, int height, int top, int bottom, int itemHeight) {
        this(width, height, top, bottom, itemHeight, 0xffffffff);
    }

    @Override
    public ITextComponent getNarration(int index, Pair<Music, UUID> t) {
        if (t.getFirst().isMetaLoaded()) {
            MusicMetaData meta = t.getFirst().getMeta();
            return new StringTextComponent(meta.title() + " - " + meta.getSource());
        } else {
            return new TranslationTextComponent("concerto.loading");
        }
    }

    public MusicWithUUIDListWidget(int width, int height, int top, int bottom, int itemHeight, int color) {
        super(width, height, top, bottom, itemHeight, color);
    }
}
