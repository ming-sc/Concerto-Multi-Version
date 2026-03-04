package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.util.ConcertoOptions;
import top.gregtao.concerto.util.GuiHelper;
import top.gregtao.concerto.util.RenderUtil;

public class ConcertoOptionsScreen extends ConcertoScreen {
    protected OptionsRowList buttonList;

    public ConcertoOptionsScreen(Screen parent) {
        super(new TranslationTextComponent("concerto.screen.options"), parent);
    }

    @Override
    protected void init() {
        this.buttonList = new OptionsRowList(this.minecraft, this.width, this.height, 18, this.height - 32, 25) {
            @Override
            public void render(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTick) {
                RenderUtil.enableScissor(x0, y0, x1, y1);
                renderListBackground(matrixStack);
                super.render(matrixStack, pMouseX, pMouseY, pPartialTick);
                RenderUtil.disableScissor();
                renderListSeparators(matrixStack);
            }

            private void renderListSeparators(MatrixStack matrixStack) {
                RenderSystem.enableBlend();
                GuiHelper.blit(matrixStack, ConcertoListWidget.INWORLD_HEADER_SEPARATOR, this.getLeft(), this.getTop() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
                GuiHelper.blit(matrixStack, ConcertoListWidget.INWORLD_FOOTER_SEPARATOR, this.getLeft(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
                RenderSystem.disableBlend();
            }

            private void renderListBackground(MatrixStack matrixStack) {
                RenderSystem.enableBlend();
                GuiHelper.blit(
                        matrixStack,
                        ConcertoListWidget.INWORLD_MENU_LIST_BACKGROUND,
                        this.getLeft(),
                        this.getTop(),
                        (float)this.getRight(),
                        (float)(this.getBottom() + (int)this.getScrollAmount()),
                        this.x1 - this.x0,
                        this.y1 - this.y0,
                        32,
                        32
                );
                RenderSystem.disableBlend();
            }
        };
        buttonList.setRenderBackground(false);
        buttonList.setRenderTopAndBottom(false);
        this.buttonList.addSmall(ConcertoOptions.INSTANCE.getOptions());
        this.addWidget(this.buttonList);
        Button resetButton = new Button(this.width / 2 - 155, this.height - 26, 150, 20,
            new TranslationTextComponent("concerto.reset"), button -> {
            if (this.minecraft != null) {
                    this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
                        ConcertoOptions.INSTANCE.resetOptions();
                        this.minecraft.setScreen(new ConcertoOptionsScreen(this.getParent()));
                    }, this.title, new TranslationTextComponent("concerto.reset_confirm")));
                }
        });
        this.addButton(resetButton);
        Button doneButton = new Button(
                this.width / 2 + 5, this.height - 26, 150, 20,
                DialogTexts.GUI_DONE, button -> this.onClose()
        );
        this.addButton(doneButton);
        super.init();
    }

    @Override
    public void removed() {
        ConcertoOptions.INSTANCE.saveOptions();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.buttonList.render(matrices, mouseX, mouseY, delta);
        InGameHudRenderer.render(matrices, mouseX, mouseY, delta);
    }
}
