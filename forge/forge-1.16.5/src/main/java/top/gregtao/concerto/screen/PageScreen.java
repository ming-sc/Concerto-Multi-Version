package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

public abstract class PageScreen extends ConcertoScreen {
    protected int page = 0, maxPage = Integer.MAX_VALUE, buttonX, buttonY, widgetWidth;

    public PageScreen(ITextComponent title, Screen parent) {
        super(title, parent);
        FontRenderer renderer = Minecraft.getInstance().font;
        this.widgetWidth = renderer.width(new TranslationTextComponent("concerto.screen.page", 999));
    }

    public PageScreen(ITextComponent title, int maxPage, Screen parent) {
        this(title, parent);
        this.maxPage = maxPage;
    }

    abstract public void onPageTurned(int page);

    /**
     * MUST BE CALLED BEFORE super.init()
     */
    private void configure(int buttonX, int buttonY) {
        this.buttonX = buttonX;
        this.buttonY = buttonY;
    }

    @Override
    protected void init() {
        this.configure(this.width / 2 - 120, this.height - 30);
        super.init();
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.previous_page"), button -> {
            if (this.page > 0) {
                this.page -= 1;
                this.onPageTurned(this.page);
            }
        }).size(20, 20).pos(this.buttonX - this.widgetWidth / 2 - 22, this.buttonY).build());
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.next_page"), button -> {
            if (this.page < this.maxPage) {
                this.page += 1;
                this.onPageTurned(this.page);
            }
        }).size(20, 20).pos(this.buttonX + this.widgetWidth / 2, this.buttonY).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        FontRenderer renderer = Minecraft.getInstance().font;
        ITextComponent text = new TranslationTextComponent("concerto.screen.page", this.page + 1);
        RenderUtil.renderCenteredString(matrices, renderer, text, this.buttonX, this.buttonY + 5, 0xffffffff);
    }
}
