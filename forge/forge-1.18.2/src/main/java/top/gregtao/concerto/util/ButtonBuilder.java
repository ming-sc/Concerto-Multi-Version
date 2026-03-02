package top.gregtao.concerto.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonBuilder {
    private final Component message;
    private final Button.OnPress onPress;
    private Button.OnTooltip onTooltip = Button.NO_TOOLTIP;
    private int x;
    private int y;
    private int width = 150;
    private int height = 20;

    public ButtonBuilder(Component pMessage, Button.OnPress pOnPress) {
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

    public ButtonBuilder tooltip(Button.OnTooltip onTooltip) {
        this.onTooltip = onTooltip;
        return this;
    }

    public Button build() {
        return new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.onTooltip);
    }
}
