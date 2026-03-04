package top.gregtao.concerto.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractTextWidget extends Widget {
    private final FontRenderer font;
    private int textColor = 16777215;

    public AbstractTextWidget(int x, int y, int width, int height, ITextComponent message, FontRenderer font) {
        super(x, y, width, height, message);
        this.font = font;
    }

    public AbstractTextWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    protected final FontRenderer getFont() {
        return this.font;
    }

    protected final int getTextColor() {
        return this.textColor;
    }
}