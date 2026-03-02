package top.gregtao.concerto.screen.netease;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import top.gregtao.concerto.core.http.netease.NeteaseCloudApiClient;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class NeteaseCloudIndexScreen extends ConcertoScreen {
    private URLImageWidget avatar;

    public NeteaseCloudIndexScreen(Screen parent) {
        super(new TranslatableComponent("concerto.screen.index.163"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.user"),
                button -> Minecraft.getInstance().setScreen(new NeteaseCloudUserScreen(this))
        ).size(100, 20).pos(this.width / 2 - 50, 40).build());
        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.search"),
                button -> Minecraft.getInstance().setScreen(new NeteaseCloudSearchScreen(this))
        ).size(100, 20).pos(this.width / 2 - 50, 65).build());

        URL avatarUrl;
        try {
            avatarUrl = (!this.loggedIn() || NeteaseCloudApiClient.LOCAL_USER.avatarUrl.isEmpty()) ? null :
                    URI.create(NeteaseCloudApiClient.LOCAL_USER.avatarUrl).toURL();
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
        return NeteaseCloudApiClient.LOCAL_USER.loggedIn;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        Component text = this.loggedIn() ? new TranslatableComponent("concerto.screen.163.welcome", NeteaseCloudApiClient.LOCAL_USER.nickname) :
                new TranslatableComponent("concerto.screen.163.not_login");
        RenderUtil.renderCenteredString(matrices, this.font, text, this.width / 2, 90, 0xffffffff);
        this.avatar.render(matrices, mouseX, mouseY, delta);
    }
}
