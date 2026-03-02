package top.gregtao.concerto.screen.kugou;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.http.kugou.KuGouMusicApiClient;
import top.gregtao.concerto.core.http.kugou.KuGouMusicUser;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.ModifiablePressableTextWidget;
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;

public class KuGouMusicIndexScreen extends ConcertoScreen {
    private URLImageWidget avatar;

    private ModifiablePressableTextWidget vipStatusWidget;

    public KuGouMusicIndexScreen(Screen parent) {
        super(new TranslatableComponent("concerto.screen.index.kugou"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.user"),
                button -> Minecraft.getInstance().setScreen(new KuGouMusicUserScreen(this))
        ).size(100, 20).pos(this.width / 2 - 50, 40).build());
        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.search"),
                button -> Minecraft.getInstance().setScreen(new KuGouMusicSearchScreen(this))
        ).size(100, 20).pos(this.width / 2 - 50, 65).build());

        URL avatarUrl;
        try {
            avatarUrl = (!this.loggedIn() || KuGouMusicApiClient.LOCAL_USER.getAvatarUrl().isEmpty()) ? null :
                    URI.create(KuGouMusicApiClient.LOCAL_USER.getAvatarUrl()).toURL();
        } catch (MalformedURLException e) {
            avatarUrl = null;
        }
        this.avatar = new URLImageWidget(64, 64, this.width / 2 - 32, 110,
                avatarUrl == null ? null : avatarUrl.toString(), false);
        ConcertoRunner.run(() -> {
            this.avatar.loadImage(true, true);
        });

        if (loggedIn() && isVersionSame()) {
            this.vipStatusWidget = this.addWidget(new ModifiablePressableTextWidget(
                    0, 0, 0, 0,
                    TextComponent.EMPTY,
                    button -> {
                        ConcertoRunner.run(() -> {
                            // 更新 VIP 状态
                            KuGouMusicUser localUser = KuGouMusicApiClient.LOCAL_USER;
                            if (localUser.isLoggedIn() && localUser.isVersionSame()) {
                                Component tip;
                                if (localUser.updateVIPStatus()) {
                                    tip = new TranslatableComponent("concerto.screen.kugou.vip.update_success");
                                } else {
                                    tip = new TranslatableComponent("concerto.screen.kugou.vip.update_failed");
                                }
                                displayAlert(tip);
                            }
                        });
                    },
                    font
            ));
        }
    }

    private boolean loggedIn() {
        return KuGouMusicApiClient.LOCAL_USER.isLoggedIn();
    }

    private boolean isVersionSame() {
        return KuGouMusicApiClient.LOCAL_USER.isVersionSame();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        Component text = this.loggedIn() ? new TranslatableComponent("concerto.screen.kugou.welcome", KuGouMusicApiClient.LOCAL_USER.getUserName()) :
                new TranslatableComponent("concerto.screen.kugou.not_login");
        RenderUtil.renderCenteredString(matrices, this.font, text, this.width / 2, 90, 0xffffffff);

        if (this.loggedIn()) {
            boolean isVersionSame = isVersionSame();
            int fontHeight = this.font.lineHeight;
            int x = 5;
            int bottom = this.height - 5;

            Component currentVersion = new TranslatableComponent("concerto.screen.kugou.version.current", getVersionName(KuGouMusicApiClient.LOCAL_USER.isLite()));
            Component apiVersion = new TranslatableComponent("concerto.screen.kugou.version.options", getVersionName(ClientConfig.INSTANCE.options.kuGouMusicLite));
            Component versionStatus = isVersionSame ?
                    new TranslatableComponent("concerto.screen.kugou.version.correct") :
                    new TranslatableComponent("concerto.screen.kugou.version.warning");

            // 避免 VIP 适用平台歧义, 只有版本匹配时才显示
            if (isVersionSame) {
                KuGouMusicUser.VIPLevel vipLevel = KuGouMusicApiClient.LOCAL_USER.getVipLevel();

                LocalDateTime vipExpireTime = KuGouMusicApiClient.LOCAL_USER.getVipExpireTime();
                if (vipLevel != KuGouMusicUser.VIPLevel.NONE && vipExpireTime != null) {
                    Component expireTime = new TranslatableComponent("concerto.screen.kugou.vip.expire_time", KuGouMusicUser.FORMATTER.format(vipExpireTime));
                    bottom -= fontHeight;
                    RenderUtil.renderText(matrices, this.font, expireTime, x, bottom, 0xffffffff, true);
                    bottom -= 1;
                }

                String levelPrefix = "concerto.screen.kugou.vip.level.";
                String levelText = new TranslatableComponent(levelPrefix + vipLevel.name().toLowerCase()).getString();
                Component vipStatus = new TranslatableComponent("concerto.screen.kugou.vip.vip_level", levelText);
                bottom -= fontHeight;
                if (vipStatusWidget != null) {
                    vipStatusWidget.x = x;
                    vipStatusWidget.y = bottom;
                    vipStatusWidget.setText(vipStatus);
                    vipStatusWidget.render(matrices, mouseX, mouseY, delta);
                }
                bottom -= 1;
            }

            RenderUtil.renderText(matrices, this.font, versionStatus, x, bottom - fontHeight, isVersionSame ? 0xff55ff55 : 0xffff5555, true);
            RenderUtil.renderText(matrices, this.font, apiVersion, x, bottom - fontHeight * 2 - 1, 0xffffffff, true);
            RenderUtil.renderText(matrices, this.font, currentVersion, x, bottom - fontHeight * 3 - 2, 0xffffffff, true);
        }

        this.avatar.render(matrices, mouseX, mouseY, delta);
    }

    public String getVersionName(boolean isLite) {
        return new TranslatableComponent("concerto.screen.kugou.version." + (isLite ? "lite" : "normal")).getString();
    }
}
