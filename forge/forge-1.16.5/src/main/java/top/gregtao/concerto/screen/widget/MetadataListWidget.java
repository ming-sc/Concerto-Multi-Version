package top.gregtao.concerto.screen.widget;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.api.WithMetaData;
import top.gregtao.concerto.core.music.meta.MetaData;
import top.gregtao.concerto.core.util.ConcertoRunner;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MetadataListWidget<T extends WithMetaData> extends ConcertoListWidget<T> {

    private final Set<T> loadingSet = ConcurrentHashMap.newKeySet();

    public MetadataListWidget(int width, int height, int top, int bottom, int itemHeight) {
        this(width, height, top, bottom, itemHeight, 0xffffffff);
    }

    public MetadataListWidget(int width, int height, int top, int bottom, int itemHeight, int color) {
        super(width, height, top, bottom, itemHeight, color);
    }

    @Override
    public ITextComponent getNarration(int index, T t) {
        if (t.isMetaLoaded()) {
            MetaData meta = t.getMeta();
            return new StringTextComponent(meta.title()).append("  ").append(new StringTextComponent(meta.author()).withStyle(TextFormatting.BOLD, TextFormatting.GRAY));
        } else {
            if (!this.loadingSet.contains(t)) {
                this.loadingSet.add(t);
                ConcertoRunner.run(t::getMeta, () -> this.loadingSet.remove(t));
            }
            return new TranslationTextComponent("concerto.loading");
        }
    }
}
