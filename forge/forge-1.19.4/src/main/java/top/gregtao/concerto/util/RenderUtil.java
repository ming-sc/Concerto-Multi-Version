package top.gregtao.concerto.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.enums.TextAlignment;

public class RenderUtil {
    public static int getTextRenderX(Component text, TextAlignment align, Font renderer, int x) {
        int realX = x, textWidth = renderer.width(text);
        if (align == TextAlignment.CENTER) {
            realX -= textWidth / 2;
        } else if (align == TextAlignment.RIGHT) {
            realX -= textWidth;
        }
        return realX;
    }

    public static void renderText(Component text, TextAlignment align, int x, int y, PoseStack matrices, Font renderer, int color) {
        int realX = getTextRenderX(text, align, renderer, x);
        if (ClientConfig.INSTANCE.options.textShadow) {
            renderer.drawShadow(matrices, text, realX, y, color);
        } else {
            renderer.draw(matrices, text, realX, y, color);
        }
    }

    public static void renderText(PoseStack poseStack, Font font, Component text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.drawShadow(poseStack, text, x, y, color);
        } else {
            font.draw(poseStack, text, x, y, color);
        }
    }

    public static void renderText(PoseStack poseStack, Font font, String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.drawShadow(poseStack, text, x, y, color);
        } else {
            font.draw(poseStack, text, x, y, color);
        }
    }

    public static void renderTextWithShadow(PoseStack poseStack, Font font, Component text, int x, int y, int color) {
        renderText(poseStack, font, text, x, y, color, true);
    }

    public static void renderCenteredString(PoseStack poseStack, Font font, Component text, int x, int y, int color) {
        int textWidth = font.width(text);
        renderTextWithShadow(poseStack, font, text, x - textWidth / 2, y, color);
    }

    public static Style getRunCommandStyle(String command) {
        return Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(command).withStyle(ChatFormatting.AQUA)));
    }
}
