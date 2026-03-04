package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

public class AcknowledgmentScreen extends ConcertoScreen {

    public AcknowledgmentScreen(Screen parent) {
        super(new TranslationTextComponent("concerto.screen.acknowledgement"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.donate.afdian"),
                button -> Util.getPlatform().openUri("https://afdian.com/a/gregtao")
        ).pos(this.width / 2 - 75, 40).size(150, 20).build());
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.donate.bilibili"),
                button -> Util.getPlatform().openUri("https://space.bilibili.com/491552285")
        ).pos(this.width / 2 - 75, 65).size(150, 20).build());
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.donate.ko-fi"),
                button -> Util.getPlatform().openUri("https://ko-fi.com/gregtao")
        ).pos(this.width / 2 - 75, 90).size(150, 20).build());
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.donate.supporters"),
                button -> Util.getPlatform().openUri("https://github.com/GregTaoo/Concerto/blob/dev/supporters.md")
        ).pos(this.width / 2 - 75, 115).size(150, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        FontRenderer renderer = Minecraft.getInstance().font;
        ITextComponent text = new TranslationTextComponent("concerto.thank_you");
        RenderUtil.renderCenteredString(matrices, renderer, text, this.width / 2, 150, 0xffffffff);
    }
}
