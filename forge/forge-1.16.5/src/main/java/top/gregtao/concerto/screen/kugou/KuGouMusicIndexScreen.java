package top.gregtao.concerto.screen.kugou;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
        super(new TranslationTextComponent("concerto.screen.index.kugou"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.user"),
                button -> Minecraft.getInstance().setScreen(new KuGouMusicUserScreen(this))
        ).size(100, 20).pos(this.width / 2 - 50, 40).build());
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.search"),
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
                    StringTextComponent.EMPTY,
                    button -> {
                        ConcertoRunner.run(() -> {
                            // 更新 VIP 状态
                            KuGouMusicUser localUser = KuGouMusicApiClient.LOCAL_USER;
                            if (localUser.isLoggedIn() && localUser.isVersionSame()) {
                                ITextComponent tip;
                                if (localUser.updateVIPStatus()) {
                                    tip = new TranslationTextComponent("concerto.screen.kugou.vip.update_success");
                                } else {
                                    tip = new TranslationTextComponent("concerto.screen.kugou.vip.update_failed");
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        ITextComponent text = this.loggedIn() ? new TranslationTextComponent("concerto.screen.kugou.welcome", KuGouMusicApiClient.LOCAL_USER.getUserName()) :
                new TranslationTextComponent("concerto.screen.kugou.not_login");
        RenderUtil.renderCenteredString(matrices, this.font, text, this.width / 2, 90, 0xffffffff);

        if (this.loggedIn()) {
            boolean isVersionSame = isVersionSame();
            int fontHeight = this.font.lineHeight;
            int x = 5;
            int bottom = this.height - 5;

            ITextComponent currentVersion = new TranslationTextComponent("concerto.screen.kugou.version.current", getVersionName(KuGouMusicApiClient.LOCAL_USER.isLite()));
            ITextComponent apiVersion = new TranslationTextComponent("concerto.screen.kugou.version.options", getVersionName(ClientConfig.INSTANCE.options.kuGouMusicLite));
            ITextComponent versionStatus = isVersionSame ?
                    new TranslationTextComponent("concerto.screen.kugou.version.correct") :
                    new TranslationTextComponent("concerto.screen.kugou.version.warning");

            // 避免 VIP 适用平台歧义, 只有版本匹配时才显示
            if (isVersionSame) {
                KuGouMusicUser.VIPLevel vipLevel = KuGouMusicApiClient.LOCAL_USER.getVipLevel();

                LocalDateTime vipExpireTime = KuGouMusicApiClient.LOCAL_USER.getVipExpireTime();
                if (vipLevel != KuGouMusicUser.VIPLevel.NONE && vipExpireTime != null) {
                    ITextComponent expireTime = new TranslationTextComponent("concerto.screen.kugou.vip.expire_time", KuGouMusicUser.FORMATTER.format(vipExpireTime));
                    bottom -= fontHeight;
                    RenderUtil.renderText(matrices, this.font, expireTime, x, bottom, 0xffffffff, true);
                    bottom -= 1;
                }

                String levelPrefix = "concerto.screen.kugou.vip.level.";
                String levelText = new TranslationTextComponent(levelPrefix + vipLevel.name().toLowerCase()).getString();
                ITextComponent vipStatus = new TranslationTextComponent("concerto.screen.kugou.vip.vip_level", levelText);
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
        return new TranslationTextComponent("concerto.screen.kugou.version." + (isLite ? "lite" : "normal")).getString();
    }
}
