package top.gregtao.concerto.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.widget.ConcertoListWidget;
import top.gregtao.concerto.screen.widget.MusicWithUUIDListWidget;
import top.gregtao.concerto.core.util.Pair;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MusicAuditionScreen extends ConcertoScreen {

    public static final Map<UUID, Music> WAIT_AUDITION = new HashMap<>();

    private MusicWithUUIDListWidget widget;

    public MusicAuditionScreen(Screen parent) {
        super(new TranslationTextComponent("concerto.screen.audition"), parent);
    }

    private static List<Pair<Music, UUID>> toPairList(Map<UUID, Music> map) {
        return map.entrySet().stream().map(entry -> Pair.of(entry.getValue(), entry.getKey())).toList();
    }

    public void refresh() {
        this.widget.reset(toPairList(WAIT_AUDITION), null);
    }

    @Override
    protected void init() {
        super.init();
        this.widget = new MusicWithUUIDListWidget(this.width, 0, 18, this.height - 35, 18);
        this.refresh();
        this.addWidget(this.widget);

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.accept"), button -> {
            ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
            ConcertoListWidget<Pair<Music, UUID>>.Entry entry = this.widget.getSelected();
            if (player != null && entry != null) {
                player.chat("/concerto-server audit " + entry.item.getSecond());
                this.widget.removeEntryWithoutScrolling(entry);
            }
        }).pos(20, this.height - 30).size(60, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.reject"), button -> {
            ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
            ConcertoListWidget<Pair<Music, UUID>>.Entry entry = this.widget.getSelected();
            if (player != null && entry != null) {
                player.chat("/concerto-server audit reject " + entry.item.getSecond());
                this.widget.removeEntryWithoutScrolling(entry);
            }
        }).pos(85, this.height - 30).size(60, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.reject.all"), button -> {
            ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
            if (player != null) {
                player.chat("/concerto-server audit reject all");
                this.widget.clear();
            }
        }).pos(150, this.height - 30).size(60, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.refresh"), button -> this.refresh())
                .pos(215, this.height - 30).size(60, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.widget.render(matrices, mouseX, mouseY, delta);
        ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
        if (player == null || !player.hasPermissions(2)) {
            RenderUtil.renderCenteredString(matrices, this.font, new TranslationTextComponent("concerto.screen.audition.permission_denied"),
                    this.width / 2, this.height / 2, 0xffffffff);
        }
    }
}
