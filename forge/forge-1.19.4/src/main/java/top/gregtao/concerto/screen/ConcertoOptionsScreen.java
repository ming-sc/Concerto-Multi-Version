package top.gregtao.concerto.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.util.ConcertoOptions;
import top.gregtao.concerto.util.GuiHelper;

public class ConcertoOptionsScreen extends ConcertoScreen {
    protected OptionsList buttonList;

    public ConcertoOptionsScreen(Screen parent) {
        super(Component.translatable("concerto.screen.options"), parent);
    }

    @Override
    protected void init() {
        this.buttonList = new OptionsList(this.minecraft, this.width, this.height, 18, this.height - 32, 25) {
            @Override
            public void render(PoseStack pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                renderListBackground(pGuiGraphics);
                super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }

            @Override
            protected void renderDecorations(PoseStack poseStack, int pMouseX, int pMouseY) {
                renderListSeparators(poseStack);
            }

            private void renderListSeparators(PoseStack poseStack) {
                RenderSystem.enableBlend();
                GuiHelper.blit(poseStack, ConcertoListWidget.INWORLD_HEADER_SEPARATOR, this.getLeft(), this.getTop() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
                GuiHelper.blit(poseStack, ConcertoListWidget.INWORLD_FOOTER_SEPARATOR, this.getLeft(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
                RenderSystem.disableBlend();
            }

            private void renderListBackground(PoseStack poseStack) {
                RenderSystem.enableBlend();
                GuiHelper.blit(
                        poseStack,
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
        this.addRenderableWidget(this.buttonList);
        Button resetButton = Button.builder(Component.translatable("concerto.reset"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
                    ConcertoOptions.INSTANCE.resetOptions();
                    this.minecraft.setScreen(new ConcertoOptionsScreen(this.getParent()));
                }, this.title, Component.translatable("concerto.reset_confirm")));
            }
        }).pos(this.width / 2 - 155, this.height - 26).build();
        this.addWidget(resetButton);
        this.addRenderableWidget(resetButton);
        Button doneButton = Button.builder(
                CommonComponents.GUI_DONE, button -> this.onClose()
        ).pos(this.width / 2 + 5, this.height - 26).build();
        this.addWidget(doneButton);
        this.addRenderableWidget(doneButton);
        super.init();
    }

    @Override
    public void removed() {
        ConcertoOptions.INSTANCE.saveOptions();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        InGameHudRenderer.render(matrices, mouseX, mouseY, delta);
    }
}
