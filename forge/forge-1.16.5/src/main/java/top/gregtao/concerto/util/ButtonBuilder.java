package top.gregtao.concerto.util;


import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class ButtonBuilder {
    private final ITextComponent message;
    private final Button.IPressable onPress;
    private Button.ITooltip onTooltip = Button.NO_TOOLTIP;
    private int x;
    private int y;
    private int width = 150;
    private int height = 20;

    public ButtonBuilder(ITextComponent pMessage, Button.IPressable pOnPress) {
        this.message = pMessage;
        this.onPress = pOnPress;
    }

    public ButtonBuilder pos(int pX, int pY) {
        this.x = pX;
        this.y = pY;
        return this;
    }

    public ButtonBuilder width(int pWidth) {
        this.width = pWidth;
        return this;
    }

    public ButtonBuilder size(int pWidth, int pHeight) {
        this.width = pWidth;
        this.height = pHeight;
        return this;
    }

    public ButtonBuilder bounds(int pX, int pY, int pWidth, int pHeight) {
        return this.pos(pX, pY).size(pWidth, pHeight);
    }

    public ButtonBuilder tooltip(Button.ITooltip onTooltip) {
        this.onTooltip = onTooltip;
        return this;
    }

    public Button build() {
        return new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.onTooltip);
    }
}
