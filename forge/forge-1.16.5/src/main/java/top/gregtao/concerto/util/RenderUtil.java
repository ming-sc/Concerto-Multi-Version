package top.gregtao.concerto.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.enums.TextAlignment;

public class RenderUtil {
    public static int getTextRenderX(ITextComponent text, TextAlignment align, FontRenderer renderer, int x) {
        int realX = x, textWidth = renderer.width(text);
        if (align == TextAlignment.CENTER) {
            realX -= textWidth / 2;
        } else if (align == TextAlignment.RIGHT) {
            realX -= textWidth;
        }
        return realX;
    }

    public static void renderText(ITextComponent text, TextAlignment align, int x, int y, MatrixStack matrices, FontRenderer renderer, int color) {
        int realX = getTextRenderX(text, align, renderer, x);
        if (ClientConfig.INSTANCE.options.textShadow) {
            renderer.drawShadow(matrices, text, realX, y, color);
        } else {
            renderer.draw(matrices, text, realX, y, color);
        }
    }

    public static void renderText(MatrixStack poseStack, FontRenderer font, ITextComponent text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.drawShadow(poseStack, text, x, y, color);
        } else {
            font.draw(poseStack, text, x, y, color);
        }
    }

    public static void renderText(MatrixStack poseStack, FontRenderer font, String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.drawShadow(poseStack, text, x, y, color);
        } else {
            font.draw(poseStack, text, x, y, color);
        }
    }

    public static void renderTextWithShadow(MatrixStack poseStack, FontRenderer font, ITextComponent text, int x, int y, int color) {
        renderText(poseStack, font, text, x, y, color, true);
    }

    public static void renderCenteredString(MatrixStack poseStack, FontRenderer font, ITextComponent text, int x, int y, int color) {
        int textWidth = font.width(text);
        renderTextWithShadow(poseStack, font, text, x - textWidth / 2, y, color);
    }

    public static Style getRunCommandStyle(String command) {
        return Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(command).withStyle(TextFormatting.AQUA)));
    }

    public static void enableScissor(int x1, int y1, int x2, int y2) {
        MainWindow window = Minecraft.getInstance().getWindow();
        int bufferHeight = window.getHeight();
        double scaleFactor = window.getGuiScale();
        int rX1 = (int) (x1 * scaleFactor);
        int rY1 = (int) (bufferHeight - y2 * scaleFactor);
        int rWidth = (int) ((x2 - x1) * scaleFactor);
        int rHeight = (int) ((y2 - y1) * scaleFactor);
        RenderSystem.enableScissor(rX1, rY1, Math.max(0, rWidth), Math.max(0, rHeight));
    }

    public static void disableScissor() {
        RenderSystem.disableScissor();
    }
}
