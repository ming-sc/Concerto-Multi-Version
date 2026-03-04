package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import top.gregtao.concerto.util.RenderUtil;

public class ModifiablePressableTextWidget extends PressableTextWidget {
    private FontRenderer fontRenderer;
    private ITextComponent text;
    private ITextComponent hoverText;

    public ModifiablePressableTextWidget(int x, int y, int width, int height, ITextComponent text, Button.IPressable onPress, FontRenderer fontRenderer) {
        super(x, y, width, height, text, onPress, fontRenderer);
        this.fontRenderer = fontRenderer;
        this.text = text;
        this.hoverText = TextComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float deltaTicks) {
        ITextComponent text = this.isHovered() ? this.hoverText : this.text;
        RenderUtil.renderTextWithShadow(matrixStack, this.fontRenderer, text, this.x, this.y, 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    public void setText(ITextComponent text) {
        this.text = text;
        this.hoverText = TextComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withUnderlined(true));
        this.setWidth(this.fontRenderer.width(text));
        this.setHeight(this.fontRenderer.lineHeight);
    }
}
