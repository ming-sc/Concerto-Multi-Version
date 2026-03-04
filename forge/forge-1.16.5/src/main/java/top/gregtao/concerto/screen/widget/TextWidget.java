package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class TextWidget extends AbstractTextWidget {

    private float horizontalAlignment;

    public TextWidget(ITextComponent message, FontRenderer fontRenderer) {
        this(0, 0, fontRenderer.width(message.getVisualOrderText()), 9, message, fontRenderer);
    }

    public TextWidget(int width, int height, ITextComponent message, FontRenderer fontRenderer) {
        this(0, 0, width, height, message, fontRenderer);
    }

    public TextWidget(int x, int y, int width, int height, ITextComponent message, FontRenderer fontRenderer) {
        super(x, y, width, height, message, fontRenderer);
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

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        ITextComponent text = this.getMessage();
        FontRenderer fontRenderer = this.getFont();
        int i = this.x + Math.round(this.horizontalAlignment * (float)(this.getWidth() - fontRenderer.width(text)));
        int var10000 = this.y;
        int var10001 = this.getHeight();
        Objects.requireNonNull(fontRenderer);
        int j = var10000 + (var10001 - 9) / 2;
        drawString(matrices, fontRenderer, text, i, j, this.getTextColor());
    }
}
