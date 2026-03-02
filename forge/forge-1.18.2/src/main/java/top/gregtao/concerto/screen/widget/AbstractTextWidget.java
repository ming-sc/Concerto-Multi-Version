package top.gregtao.concerto.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class AbstractTextWidget extends AbstractWidget {
    private final Font font;
    private int textColor = 16777215;

    public AbstractTextWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message);
        this.font = font;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    public AbstractTextWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    protected final Font getFont() {
        return this.font;
    }

    protected final int getTextColor() {
        return this.textColor;
    }
}