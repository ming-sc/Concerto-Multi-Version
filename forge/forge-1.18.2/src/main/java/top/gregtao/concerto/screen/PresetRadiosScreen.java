package top.gregtao.concerto.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.api.WithMetaData;
import top.gregtao.concerto.core.music.list.Playlist;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.MetadataListWidget;
import top.gregtao.concerto.util.ButtonBuilder;

public class PresetRadiosScreen extends ConcertoScreen {

    private MetadataListWidget<Playlist> playlistList;

    private <T extends WithMetaData> MetadataListWidget<T> initWidget() {
        return new MetadataListWidget<>(this.width, 0, 18, this.height - 35, 18) {
            @Override
            public void onDoubleClicked(ConcertoListWidget<T>.Entry entry) {
                Minecraft.getInstance().setScreen(new PlaylistPreviewScreen((Playlist) entry.item, PresetRadiosScreen.this));
            }
        };
    }

    public PresetRadiosScreen(Screen parent) {
        super(new TranslatableComponent("concerto.screen.preset_radios"), parent);
    }

    public void reset() {
        this.playlistList.reset(ConcertoClient.presetRadios, null);
    }

    @Override
    protected void init() {
        super.init();
        this.playlistList = this.initWidget();
        this.reset();
        this.addWidget(this.playlistList);
        this.addRenderableWidget(this.playlistList);

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.play"), button -> {
            ConcertoListWidget<Playlist>.Entry entry = this.playlistList.getSelected();
            if (entry != null) {
                Minecraft.getInstance().setScreen(new PlaylistPreviewScreen(entry.item, this));
            }
        }).pos(20, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.refresh"), button -> {
            LocalPlayer player = PlayerUtil.getLocalPlayer();
            if (player != null) {
                player.chat("/concerto-server fetch-radios");
            }
        }).pos(85, this.height - 30).size(60, 20).build());
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.playlistList.render(matrices, mouseX, mouseY, delta);
    }
}
