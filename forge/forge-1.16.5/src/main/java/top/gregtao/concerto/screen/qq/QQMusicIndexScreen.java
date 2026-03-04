package top.gregtao.concerto.screen.qq;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.http.qq.QQMusicApiClient;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class QQMusicIndexScreen extends ConcertoScreen {
    private URLImageWidget avatar;

    public QQMusicIndexScreen(Screen parent) {
        super(new TranslationTextComponent("concerto.screen.index.qq"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.user"),
                button -> Minecraft.getInstance().setScreen(this.loggedIn() ? new QQMusicUserScreen(this) : new QQMusicLoginScreens(this))
        ).size(100, 20).pos(this.width / 2 - 50, 40).build());
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.search"),
                button -> Minecraft.getInstance().setScreen(new QQMusicSearchScreen(this))
        ).size(100, 20).pos(this.width / 2 - 50, 65).build());

        URL avatarUrl;
        try {
            avatarUrl = (!this.loggedIn() || QQMusicApiClient.LOCAL_USER.avatarUrl.isEmpty()) ? null :
                    URI.create(QQMusicApiClient.LOCAL_USER.avatarUrl).toURL();
        } catch (MalformedURLException e) {
            avatarUrl = null;
        }
        this.avatar = new URLImageWidget(64, 64, this.width / 2 - 32, 110,
                avatarUrl == null ? null : avatarUrl.toString(), false);
        ConcertoRunner.run(() -> this.avatar.loadImage(true, true));
    }

    @Override
    public void onClose() {
        super.onClose();
        this.avatar.close();
    }

    private boolean loggedIn() {
        return QQMusicApiClient.LOCAL_USER.loggedIn;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        ITextComponent text = this.loggedIn() ? new TranslationTextComponent("concerto.screen.qq.welcome", QQMusicApiClient.LOCAL_USER.nickname) :
                new TranslationTextComponent("concerto.screen.qq.not_login");
        RenderUtil.renderCenteredString(matrices, this.font, text, this.width / 2, 90, 0xffffffff);
        this.avatar.render(matrices, mouseX, mouseY, delta);
    }
}
