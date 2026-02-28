package top.gregtao.concerto.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.gregtao.concerto.core.enums.OrderType;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.GeneralPlaylistWidget;

public class GeneralPlaylistScreen extends ApplyDraggedFileScreen {
    private GeneralPlaylistWidget widget;
    protected EditBox searchBox;

    public GeneralPlaylistScreen(Screen parent) {
        super(Component.translatable("concerto.screen.general_list"), parent);
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
        this.widget = new GeneralPlaylistWidget(this.width, this.height - 75, 40, 18);

        this.addWidget(this.widget);

        this.searchBox = new EditBox(this.font, this.width / 2 - 185, 18, 300, 18,
                this.searchBox, Component.translatable("concerto.screen.search"));
        this.addWidget(this.searchBox);
        this.addRenderableWidget(this.searchBox);

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.search"), button ->
                this.toggleSearch()).pos(this.width / 2 + 125, 17).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.next"), button -> {
            if (!MusicPlayer.INSTANCE.started) MusicPlayer.INSTANCE.start();
            else if (!MusicPlayer.INSTANCE.playNextLock.get()) MusicPlayer.INSTANCE.playNext(1, index -> {
                this.widget.reset();
                this.widget.setSelected(index);
            });
        }).pos(this.width / 2 - 185, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.play"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                MusicPlayer.INSTANCE.skipTo(entry.index);
            } else if (!MusicPlayer.INSTANCE.started) {
                MusicPlayer.INSTANCE.start();
            }
        }).pos(this.width / 2 - 135, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.delete"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                MusicPlayer.INSTANCE.remove(entry.index, () -> this.widget.removeEntryFromTop(entry));
            }
        }).pos(this.width / 2 - 85, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(CycleButton.<OrderType>builder((type) -> Component.literal(type.getI18nString())).withValues(OrderType.values())
                .withInitialValue(MusicPlayerHandler.INSTANCE.getOrderType()).create(
                        this.width / 2 - 35, this.height - 30, 60, 20, Component.translatable("concerto.screen.order"),
                        (widget, orderType) -> MusicPlayerHandler.INSTANCE.setOrderType(orderType)));

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.pause"), button -> {
            if (MusicPlayer.INSTANCE.started) {
                if (MusicPlayer.INSTANCE.forcePaused) MusicPlayer.INSTANCE.forceResume();
                else MusicPlayer.INSTANCE.forcePause();
            }
        }).pos(this.width / 2 + 25, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.info"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                Minecraft.getInstance().setScreen(new MusicInfoScreen(entry.item, this));
            }
        }).pos(this.width / 2 + 75, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.clear"), button -> {
            MusicPlayer.INSTANCE.clear();
            Minecraft.getInstance().setScreen(null);
        }).pos(this.width / 2 + 125, this.height - 30).size(50, 20).build());
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.widget.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && this.searchBox.isHoveredOrFocused()) {
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
