package top.gregtao.concerto.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.MusicWithUUIDListWidget;
import top.gregtao.concerto.core.util.Pair;
import top.gregtao.concerto.util.ButtonBuilder;

import java.util.UUID;

public class MusicConfirmationScreen extends ConcertoScreen {

    private MusicWithUUIDListWidget widget;

    public MusicConfirmationScreen(Screen parent) {
        super(new TranslatableComponent("concerto.screen.confirmation"), parent);
    }

    public void refresh() {
        this.widget.reset(ClientMusicNetworkHandler.WAIT_CONFIRMATION.entrySet().stream().map(
                entry -> Pair.of(entry.getValue().music, entry.getKey())).toList(), null);
    }

    @Override
    protected void init() {
        super.init();
        this.widget = new MusicWithUUIDListWidget(this.width, 0, 18, this.height - 35, 18);
        this.refresh();
        this.addWidget(this.widget);

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.accept"), button -> {
            LocalPlayer player = PlayerUtil.getLocalPlayer();
            ConcertoListWidget<Pair<Music, UUID>>.Entry entry = this.widget.getSelected();
            if (player != null && entry != null) {
                player.chat("/sharemusic accept " + entry.item.getSecond());
                this.widget.removeEntryWithoutScrolling(entry);
            }
        }).pos(20, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.reject"), button -> {
            LocalPlayer player = PlayerUtil.getLocalPlayer();
            ConcertoListWidget<Pair<Music, UUID>>.Entry entry = this.widget.getSelected();
            if (player != null && entry != null) {
                player.chat("/sharemusic reject " + entry.item.getSecond());
                this.widget.removeEntryWithoutScrolling(entry);
            }
        }).pos(85, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.reject.all"), button -> {
            LocalPlayer player = PlayerUtil.getLocalPlayer();
            if (player != null) {
                player.chat("/sharemusic reject all");
                this.widget.clear();
            }
        }).pos(150, this.height - 30).size(60, 20).build());

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.refresh"), button -> this.refresh())
                .pos(215, this.height - 30).size(60, 20).build());
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.widget.render(matrices, mouseX, mouseY, delta);
    }
}
