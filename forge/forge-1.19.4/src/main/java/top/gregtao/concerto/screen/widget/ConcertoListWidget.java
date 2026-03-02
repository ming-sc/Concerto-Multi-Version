package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.util.GuiHelper;
import top.gregtao.concerto.util.RenderUtil;

import java.util.List;
import java.util.ListIterator;

public class ConcertoListWidget<T> extends ObjectSelectionList<ConcertoListWidget<T>.Entry> {
    public static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND = ResourceLocation.fromNamespaceAndPath(ConcertoClient.MOD_ID, "textures/gui/inworld_menu_list_background.png");
    public static final ResourceLocation INWORLD_HEADER_SEPARATOR = ResourceLocation.fromNamespaceAndPath(ConcertoClient.MOD_ID, "textures/gui/inworld_header_separator.png");
    public static final ResourceLocation INWORLD_FOOTER_SEPARATOR = ResourceLocation.fromNamespaceAndPath(ConcertoClient.MOD_ID, "textures/gui/inworld_footer_separator.png");
    private int color = 0xffffffff;

    public ConcertoListWidget(int width, int height, int top, int bottom, int itemHeight) {
        super(Minecraft.getInstance(), width, height, top, bottom, itemHeight);
        setRenderBackground(false);
        setRenderTopAndBottom(false);
    }

    public ConcertoListWidget(int width, int height, int top, int bottom, int itemHeight, int color) {
        this(width, height, top, bottom, itemHeight);
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
    public boolean removeEntryFromTop(Entry entry) {
        ListIterator<Entry> iterator = this.children().listIterator(entry.entryIndex + 1);
        while (iterator.hasNext()) {
            iterator.next().index--;
        }
        return super.removeEntryFromTop(entry);
    }

    @Override
    public int getRowWidth() {
        return this.width - 35;
    }

    @Override
    protected int getScrollbarPosition() {
        return getDefaultScrollbarPosition();
    }

    private int getRealRowLeft() {
        return this.getLeft() + this.width / 2 - this.getRowWidth() / 2;
    }

    private int getRealRowRight() {
        return this.getRealRowLeft() + this.getRowWidth();
    }

    protected int getDefaultScrollbarPosition() {
        return this.getRealRowRight() + this.getListOutlinePadding();
    }

    private int getListOutlinePadding() {
        return 10;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderListBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void renderDecorations(PoseStack poseStack, int pMouseX, int pMouseY) {
        renderListSeparators(poseStack);
    }

    private void renderListSeparators(PoseStack poseStack) {
        RenderSystem.enableBlend();
        GuiHelper.blit(poseStack, INWORLD_HEADER_SEPARATOR, this.getLeft(), this.getTop() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
        GuiHelper.blit(poseStack, INWORLD_FOOTER_SEPARATOR, this.getLeft(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
        RenderSystem.disableBlend();
    }

    protected void renderListBackground(PoseStack poseStack) {
        RenderSystem.enableBlend();
        GuiHelper.blit(
                poseStack,
                INWORLD_MENU_LIST_BACKGROUND,
                this.getLeft(),
                this.getTop(),
                (float)this.getRight(),
                (float)(this.getBottom() + (int)this.getScrollAmount()),
                this.x1 - this.x0,
                this.y1 - this.y0,
                32,
                32
        );
        RenderSystem.disableBlend();
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
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
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
        public void render(PoseStack stack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            RenderUtil.renderText(stack, Minecraft.getInstance().font, this.getNarration(), x, y + 3, ConcertoListWidget.this.color, false);
        }
    }
}
