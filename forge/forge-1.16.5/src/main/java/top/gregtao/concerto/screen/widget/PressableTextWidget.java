package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;


public class PressableTextWidget extends Button {
    private final FontRenderer fontRenderer;
    private final ITextComponent text;
    private final ITextComponent hoverText;

    public PressableTextWidget(int x, int y, int width, int height, ITextComponent text, IPressable onPress, FontRenderer fontRenderer) {
        super(x, y, width, height, text, onPress);
        this.fontRenderer = fontRenderer;
        this.text = text;
        this.hoverText = TextComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        ITextComponent text = this.isHovered() ? this.hoverText : this.text;
        drawString(matrices, this.fontRenderer, text, this.x, this.y, 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}