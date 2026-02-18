package top.gregtao.concerto.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.music.meta.music.MusicMetaData;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.screen.widget.URLImageWidget;

public class MusicInfoScreen extends ConcertoScreen {

    private URLImageWidget headPicture;
    private final Music music;

    public MusicInfoScreen(Music music, Screen parent) {
        super(Component.translatable("concerto.screen.info"), parent);
        this.music = music;
    }

    @Override
    protected void init() {
        super.init();
        this.headPicture = new URLImageWidget(140, 140, this.width / 2 - 145, this.height / 2 - 70, null);

        ConcertoRunner.run(() -> {
            this.music.getMeta();
            this.initInfo();
        });

        Button requestButton = Button.builder(
                Component.translatable("concerto.screen.request"),
                button -> ClientMusicNetworkHandler.musicAgentAddMusic(this.music)
        ).pos(this.width - 245, this.height - 30).size(50, 20).build();
        this.addRenderableWidget(requestButton);
        requestButton.active = ConcertoClient.clientState == ConcertoClient.ClientState.MUSIC_AGENT;

        this.addRenderableWidget(Button.builder(
                Component.translatable("concerto.screen.play"),
                button -> MusicPlayer.INSTANCE.addMusicHere(this.music, true)
        ).pos(this.width - 190, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("concerto.screen.add"),
                button -> MusicPlayer.INSTANCE.addMusic(this.music)
        ).pos(this.width - 135, this.height - 30).size(50, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("concerto.screen.copy_link"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.keyboardHandler.setClipboard(this.music.getLink());
                    }
                }
        ).pos(this.width - 80, this.height - 30).size(50, 20).build());
    }

    private void initInfo() {
        MusicMetaData meta = this.music.getMeta();
        if (!meta.headPictureUrl().isEmpty()) {
            this.headPicture.setUrl(meta.headPictureUrl());
            this.headPicture.loadImage();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.headPicture.close();
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.headPicture.render(matrices, mouseX, mouseY, delta);
        matrices.drawString(this.font, this.music.getMeta().getSource(), this.width / 2 + 5, this.height / 2 - 20, 0xffffffff, false);
        matrices.drawString(this.font, this.music.getMeta().title(), this.width / 2 + 5, this.height / 2 - 5, 0xffffffff, false);
        matrices.drawString(this.font, this.music.getMeta().author(), this.width / 2 + 5, this.height / 2 + 10, 0xffffffff, false);
    }
}
