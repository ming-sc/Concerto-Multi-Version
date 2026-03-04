package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import top.gregtao.concerto.util.RenderUtil;

public class ModifiablePressableTextWidget extends PressableTextWidget {
    private Font textRenderer;
    private Component text;
    private Component hoverText;

    public ModifiablePressableTextWidget(int x, int y, int width, int height, Component text, Button.OnPress onPress, Font textRenderer) {
        super(x, y, width, height, text, onPress, textRenderer);
        this.textRenderer = textRenderer;
        this.text = text;
        this.hoverText = ComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float deltaTicks) {
        Component text = this.isHovered() ? this.hoverText : this.text;
        RenderUtil.renderTextWithShadow(poseStack, this.textRenderer, text, this.x, this.y, 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void setText(Component text) {
        this.text = text;
        this.hoverText = ComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withUnderlined(true));
        this.setWidth(this.textRenderer.width(text));
        this.setHeight(this.textRenderer.lineHeight);
    }
}
