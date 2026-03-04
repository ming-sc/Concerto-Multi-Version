package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import top.gregtao.concerto.core.enums.OrderType;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.CyclingButtonWidget;
import top.gregtao.concerto.screen.widget.GeneralPlaylistWidget;
import top.gregtao.concerto.util.ButtonBuilder;

public class GeneralPlaylistScreen extends ApplyDraggedFileScreen {
    private GeneralPlaylistWidget widget;
    protected TextFieldWidget searchBox;

    public GeneralPlaylistScreen(Screen parent) {
        super(new TranslationTextComponent("concerto.screen.general_list"), parent);
    }

    public void toggleSearch() {
        if (!this.searchBox.getValue().isEmpty()) {
            this.widget.reset(this.searchBox.getValue());
        } else {
            this.widget.reset();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.widget = new GeneralPlaylistWidget(this.width, this.height, 40, this.height - 35, 18);
        this.addWidget(this.widget);

        this.searchBox = new TextFieldWidget(this.font, this.width / 2 - 185, 18, 300, 18,
                this.searchBox, new TranslationTextComponent("concerto.screen.search"));
        this.addButton(this.searchBox);

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.search"), button ->
                this.toggleSearch()).pos(this.width / 2 + 125, 17).size(50, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.next"), button -> {
            if (!MusicPlayer.INSTANCE.started) MusicPlayer.INSTANCE.start();
            else if (!MusicPlayer.INSTANCE.playNextLock.get()) MusicPlayer.INSTANCE.playNext(1, index -> {
                this.widget.reset();
                this.widget.setSelected(index);
            });
        }).pos(this.width / 2 - 185, this.height - 30).size(50, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.play"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                MusicPlayer.INSTANCE.skipTo(entry.index);
            } else if (!MusicPlayer.INSTANCE.started) {
                MusicPlayer.INSTANCE.start();
            }
        }).pos(this.width / 2 - 135, this.height - 30).size(50, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.delete"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                MusicPlayer.INSTANCE.remove(entry.index, () -> this.widget.removeEntryWithoutScrolling(entry));
            }
        }).pos(this.width / 2 - 85, this.height - 30).size(50, 20).build());

        this.addButton(CyclingButtonWidget.<OrderType>builder((type) -> new StringTextComponent(type.getI18nString())).values(OrderType.values())
                .initially(MusicPlayerHandler.INSTANCE.getOrderType()).build(
                        this.width / 2 - 35, this.height - 30, 60, 20, new TranslationTextComponent("concerto.screen.order"),
                        (widget, orderType) -> MusicPlayerHandler.INSTANCE.setOrderType(orderType)));

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.pause"), button -> {
            if (MusicPlayer.INSTANCE.started) {
                if (MusicPlayer.INSTANCE.forcePaused) MusicPlayer.INSTANCE.forceResume();
                else MusicPlayer.INSTANCE.forcePause();
            }
        }).pos(this.width / 2 + 25, this.height - 30).size(50, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.info"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                Minecraft.getInstance().setScreen(new MusicInfoScreen(entry.item, this));
            }
        }).pos(this.width / 2 + 75, this.height - 30).size(50, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.clear"), button -> {
            MusicPlayer.INSTANCE.clear();
            Minecraft.getInstance().setScreen(null);
        }).pos(this.width / 2 + 125, this.height - 30).size(50, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.widget.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && this.searchBox.isHovered()) {
            this.toggleSearch();
            return true;
        }
        return this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.searchBox.charTyped(chr, modifiers);
    }
}
