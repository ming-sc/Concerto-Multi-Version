package top.gregtao.concerto.screen.widget;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.ListIterator;

public class ConcertoListWidget<T> extends ObjectSelectionList<ConcertoListWidget<T>.Entry> {
    private int color = 0xffffffff;

    public ConcertoListWidget(int width, int height, int top, int itemHeight) {
        super(Minecraft.getInstance(), width, height, top, itemHeight);
    }

    public ConcertoListWidget(int width, int height, int top, int itemHeight, int color) {
        this(width, height, top, itemHeight);
        this.color = color;
    }

    public Component getNarration(int index, T t) {
        return Component.literal(String.valueOf(index));
    }

    public void onDoubleClicked(Entry entry) {}

    public void reset(List<T> list, T selected, String key) {
        this.clearEntries();
        key = key.toLowerCase();
        for (int i = 0, j = 0; i < list.size(); ++i) {
            T music = list.get(i);
            if (key.isEmpty() || this.getNarration(i, music).getString().toLowerCase().matches(".*" + key + ".*")) {
                Entry entry = new Entry(music, i, j++);
                this.addEntry(entry);
                if (music == selected) {
                    this.setSelected(entry);
                    this.centerScrollOn(entry);
                }
            }
        }
    }

    public void reset(List<T> list, T selected) {
        this.reset(list, selected, "");
    }

    public void setSelected(int index) {
        Entry entry = this.children().get(index);
        this.setSelected(entry);
        this.centerScrollOn(entry);
    }

    public void clear() {
        super.clearEntries();
    }

    @Override
    public void removeEntryFromTop(Entry entry) {
        ListIterator<Entry> iterator = this.children().listIterator(entry.entryIndex + 1);
        while (iterator.hasNext()) {
            iterator.next().index--;
        }
        super.removeEntryFromTop(entry);
    }

    @Override
    public int getRowWidth() {
        return this.width - 35;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return super.keyPressed(event);
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        public T item;
        public int index, entryIndex;
        private long lastClickTime = 0;

        public Entry(T item, int index, int entryIndex) {
            this.item = item;
            this.index = index;
            this.entryIndex = entryIndex;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            if (event.button() == 0) {
                if (Util.getMillis() - this.lastClickTime < 250) {
                    ConcertoListWidget.this.onDoubleClicked(this);
                } else {
                    ConcertoListWidget.this.setSelected(this);
                }
                this.lastClickTime = Util.getMillis();
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return ConcertoListWidget.this.getNarration(this.index, this.item);
        }

        @Override
        public void renderContent(GuiGraphics context, int i, int i1, boolean b, float v) {
            context.drawString(Minecraft.getInstance().font, this.getNarration(), getContentX(), getContentY() + 3, ConcertoListWidget.this.color, false);
        }
    }
}
