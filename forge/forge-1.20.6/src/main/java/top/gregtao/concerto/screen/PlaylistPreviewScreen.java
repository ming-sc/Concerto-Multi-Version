package top.gregtao.concerto.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.music.list.Playlist;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.MetadataListWidget;
import top.gregtao.concerto.core.util.ConcertoRunner;

public class PlaylistPreviewScreen extends ConcertoScreen {
    private final Playlist playlist;
    private MetadataListWidget<Music> widget;

    public PlaylistPreviewScreen(Playlist playlist, Screen parent) {
        super(Component.literal(Component.translatable("concerto." + (playlist.isAlbum() ? "album" : "playlist")).getString() +
                ": " + playlist.getMeta().title() + " - " + playlist.getMeta().author()), parent);
        this.playlist = playlist;
    }

    @Override
    protected void init() {
        super.init();
        this.widget = new MetadataListWidget<>(this.width, this.height - 55, 20, 18) {
            @Override
            public void onDoubleClicked(ConcertoListWidget<Music>.Entry entry) {
                MusicPlayer.INSTANCE.addMusicHere(entry.item, true);
            }
        };
        this.addWidget(this.widget);
        ConcertoRunner.run(() -> this.widget.reset(this.playlist.getList(), null));

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.playlist.add"), button ->
            MusicPlayer.INSTANCE.addMusic(this.playlist.getList(), () ->
                    MusicPlayer.INSTANCE.skipTo(MusicPlayerHandler.INSTANCE.getMusicList().size() - this.playlist.getList().size())
        )).pos(20, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.play"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                MusicPlayer.INSTANCE.addMusicHere(entry.item, true);
            }
        }).pos(85, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.add"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                MusicPlayer.INSTANCE.addMusic(entry.item);
            }
        }).pos(150, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.screen.info"), button -> {
            ConcertoListWidget<Music>.Entry entry = this.widget.getSelected();
            if (entry != null) {
                Minecraft.getInstance().setScreen(new MusicInfoScreen(entry.item, this));
            }
        }).pos(215, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("concerto.playlist.export"), button -> {
            Component text = PresetPlaylistsConfig.saveToLocalPlaylists(this.playlist) ? Component.translatable("concerto.playlist.export.success") :
                    Component.translatable("concerto.playlist.export.fail");
            this.displayAlert(text);
        }).pos(280, this.height - 30).size(60, 20).build());
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        matrices.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xffffffff);
        this.widget.render(matrices, mouseX, mouseY, delta);
    }
}
