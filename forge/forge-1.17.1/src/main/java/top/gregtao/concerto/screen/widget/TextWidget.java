package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class TextWidget extends AbstractTextWidget {

    private float horizontalAlignment;

    public TextWidget(Component message, Font font) {
        this(0, 0, font.width(message.getVisualOrderText()), 9, message, font);
    }

    public TextWidget(int width, int height, Component message, Font font) {
        this(0, 0, width, height, message, font);
    }

    public TextWidget(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);
        this.horizontalAlignment = 0.5F;
        this.active = false;
    }

    public TextWidget setTextColor(int textColor) {
        super.setTextColor(textColor);
        return this;
    }

    private TextWidget align(float horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public TextWidget alignLeft() {
        return this.align(0.0F);
    }

    public TextWidget alignCenter() {
        return this.align(0.5F);
    }

    public TextWidget alignRight() {
        return this.align(1.0F);
    }

    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Component text = this.getMessage();
        Font font = this.getFont();
        int i = this.x + Math.round(this.horizontalAlignment * (float)(this.getWidth() - font.width(text)));
        int var10000 = this.y;
        int var10001 = this.getHeight();
        Objects.requireNonNull(font);
        int j = var10000 + (var10001 - 9) / 2;
        drawString(matrices, font, text, i, j, this.getTextColor());
    }
}
