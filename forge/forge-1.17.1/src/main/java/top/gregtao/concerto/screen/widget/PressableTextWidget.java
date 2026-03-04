package top.gregtao.concerto.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;


public class PressableTextWidget extends Button {
    private final Font font;
    private final Component text;
    private final Component hoverText;

    public PressableTextWidget(int x, int y, int width, int height, Component text, OnPress onPress, Font font) {
        super(x, y, width, height, text, onPress);
        this.font = font;
        this.text = text;
        this.hoverText = ComponentUtils.mergeStyles(text.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Component text = this.isHovered() ? this.hoverText : this.text;
        drawString(matrices, this.font, text, this.x, this.y, 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}