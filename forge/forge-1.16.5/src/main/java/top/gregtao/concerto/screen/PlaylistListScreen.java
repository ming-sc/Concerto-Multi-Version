package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.music.list.Playlist;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.MetadataListWidget;
import top.gregtao.concerto.util.ButtonBuilder;

import java.util.List;

public class PlaylistListScreen extends ConcertoScreen {
    private final List<Playlist> playlists;
    private MetadataListWidget<Playlist> playlistList;

    public PlaylistListScreen(ITextComponent title, Screen parent, List<Playlist> playlists) {
        super(title, parent);
        this.playlists = playlists;
    }

    @Override
    protected void init() {
        super.init();
        this.playlistList = new MetadataListWidget<>(this.width, 0, 18, this.height - 35, 18) {
            @Override
            public void onDoubleClicked(ConcertoListWidget<Playlist>.Entry entry) {
                Minecraft.getInstance().setScreen(new PlaylistPreviewScreen(entry.item, PlaylistListScreen.this));
            }
        };
        this.playlistList.reset(this.playlists, null, "");
        this.addWidget(playlistList);

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.play"), button -> {
            ConcertoListWidget<Playlist>.Entry entry = this.playlistList.getSelected();
            if (entry != null) {
                Minecraft.getInstance().setScreen(new PlaylistPreviewScreen(entry.item, this));
            }
        }).pos(20, this.height - 30).size(60, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.playlistList.render(matrices, mouseX, mouseY, delta);
    }
}
