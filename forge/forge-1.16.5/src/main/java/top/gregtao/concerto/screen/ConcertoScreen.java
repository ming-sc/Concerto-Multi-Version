package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.*;
import top.gregtao.concerto.screen.widget.PressableTextWidget;
import top.gregtao.concerto.util.RenderUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConcertoScreen extends Screen {
    private final Screen parent;
    private ITextComponent message;
    private boolean messageVisible = false;

    public ConcertoScreen(ITextComponent title, Screen parent) {
        super(title.copy().withStyle(Style.EMPTY.withColor(TextFormatting.DARK_AQUA)));
        this.parent = parent;
    }

    public void removeWidget(IGuiEventListener element) {
        this.children.remove(element);
    }

    public void displayAlert(ITextComponent text) {
        this.message = text;
        this.messageVisible = true;
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            this.message = StringTextComponent.EMPTY;
            this.messageVisible = false;
        });
    }

    @Override
    protected void init() {
        super.init();

        if (!(this instanceof AcknowledgmentScreen)) {
            ITextComponent text = new TranslationTextComponent("concerto.donate");
            int width = this.font.width(text);
            this.addButton(
                    new PressableTextWidget(this.width - 5 - width, this.height - 5 - this.font.lineHeight, width,
                            this.font.lineHeight,
                            text, button -> Minecraft.getInstance().setScreen(new AcknowledgmentScreen(this)),
                            this.font)
            );
        }

       this.messageVisible = false;
    }

    protected Screen getParent() {
        return this.parent;
    }


    @Override
    public void onClose() {
        for (IGuiEventListener element : this.children()) {
            if (element instanceof Closeable closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.onClose();
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        RenderUtil.renderCenteredString(matrices, this.font, this.title, this.width / 2, 5, 0xffffffff);
        if (this.messageVisible) {
            this.font.draw(matrices, this.message, (float) (this.width - this.font.width(this.message)) / 2,
                    (float) this.height / 2 - 10, 0xffffffff);
        }
    }
}
