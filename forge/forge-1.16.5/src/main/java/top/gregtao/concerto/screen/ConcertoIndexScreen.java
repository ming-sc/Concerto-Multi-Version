package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.network.room.MusicRoom;
import top.gregtao.concerto.screen.kugou.KuGouMusicIndexScreen;
import top.gregtao.concerto.screen.netease.NeteaseCloudIndexScreen;
import top.gregtao.concerto.screen.qq.QQMusicIndexScreen;
import top.gregtao.concerto.screen.widget.PressableTextWidget;
import top.gregtao.concerto.screen.widget.TextWidget;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

public class ConcertoIndexScreen extends ConcertoScreen {
    public ConcertoIndexScreen(Screen parent) {
        super(new TranslationTextComponent("concerto.screen.index.title"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.general_list"),
                button -> Minecraft.getInstance().setScreen(new GeneralPlaylistScreen(this))
        ).pos(this.width / 2 - 120, 20).size(115, 20).build());

        Button widget = new ButtonBuilder(new TranslationTextComponent("concerto.screen.audition"),
                button -> Minecraft.getInstance().setScreen(new MusicAuditionScreen(this))
        ).pos(this.width / 2 + 5, 20).size(115, 20).build();
        this.addButton(widget);
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null || !player.hasPermissions(2) || !ConcertoClient.isServerAvailable()) {
            widget.active = false;
        }

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.confirmation"),
                button -> Minecraft.getInstance().setScreen(new MusicConfirmationScreen(this))
        ).pos(this.width / 2 + 5, 50).size(115, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.index.163"),
                button -> Minecraft.getInstance().setScreen(new NeteaseCloudIndexScreen(this))
        ).pos(this.width / 2 - 120, 50).size(115, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.index.qq"),
                button -> Minecraft.getInstance().setScreen(new QQMusicIndexScreen(this))
        ).pos(this.width / 2 - 120, 80).size(115, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.index.kugou"),
                button -> Minecraft.getInstance().setScreen(new KuGouMusicIndexScreen(this))
        ).pos(this.width / 2 - 120, 110).size(115, 20).build());

        Button widget1 = new ButtonBuilder(new TranslationTextComponent("concerto.screen.preset_radios"),
                button -> Minecraft.getInstance().setScreen(new PresetRadiosScreen(this))
        ).pos(this.width / 2 + 5, 80).size(115, 20).build();
        this.addButton(widget1);
        if (player == null || !ConcertoClient.isServerAvailable()) {
            widget1.active = false;
        }

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.add"),
                button -> Minecraft.getInstance().setScreen(new AddMusicScreen(this)))
                .pos(this.width / 2 - 120, 140).size(115, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.local_playlists"),
                button -> Minecraft.getInstance().setScreen(new PlaylistListScreen(
                        new TranslationTextComponent("concerto.screen.local_playlists"), this, PresetPlaylistsConfig.LOCAL_PLAYLISTS.getRadios()))
        ).pos(this.width / 2 + 5, 110).size(115, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.report_bugs"),
                button -> Util.getPlatform().openUri("https://github.com/GregTaoo/Concerto/issues")
        ).pos(this.width / 2 + 5, 140).size(115, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.options"),
                button -> Minecraft.getInstance().setScreen(new ConcertoOptionsScreen(this))
        ).pos(this.width / 2 - 120, 170).size(115, 20).build());

        if (this.minecraft != null) {
            switch (ConcertoClient.clientState) {
                case MUSIC_ROOM -> {
                    String uuid = MusicRoom.CLIENT_ROOM.uuid.toString();
                    ITextComponent text = new TranslationTextComponent("concerto.screen.in_music_room", uuid);
                    int width = this.font.width(text);
                    this.addButton(new PressableTextWidget(
                            (this.width - width) / 2, 215, width,
                            this.font.lineHeight, text,
                            button -> this.minecraft.keyboardHandler.setClipboard(uuid),
                            this.font
                    ));
                }
                case MUSIC_AGENT -> {
                    ITextComponent text = new TranslationTextComponent("concerto.screen.in_music_agent");
                    int width = this.font.width(text);
                    this.addButton(new TextWidget(
                            (this.width - width) / 2, 215, width,
                            this.font.lineHeight, text,
                            this.font
                    ));
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (ConcertoClient.clientState != ConcertoClient.ClientState.LOCAL) {
            RenderUtil.renderCenteredString(
                    matrices,
                    this.font,
                    new TranslationTextComponent("concerto.screen.in_which_room"),
                    this.width / 2, 200, 0xffffffff
            );
        }
    }
}
