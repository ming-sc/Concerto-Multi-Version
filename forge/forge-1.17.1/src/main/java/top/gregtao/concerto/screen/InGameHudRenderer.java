package top.gregtao.concerto.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.util.RenderUtil;
import top.gregtao.concerto.util.Vector2i;

public class InGameHudRenderer {

    public static ScrollingText MUSIC_DETAIL_SCROLL = new ScrollingText();

    public static URLImageWidget HEAD_PICTURE = new URLImageWidget(20, 20, 0, 0, null, false);

    public static void init() {
        MusicPlayerHandler.headPictureSetter = (url) -> {
            HEAD_PICTURE.setUrl(url);
            HEAD_PICTURE.loadImage(true, ClientConfig.INSTANCE.options.coverImgInCircle);
        };
    }

    public static class ScrollingText {
        public static int STOP_TICKS = 180;

        private int width = 0, maxWidth = 0;
        private float dx = 0, stopTicks = 0;
        private boolean stop = false, go_back = false;

        private long lastRenderTime = 0;

        private void reset() {
            this.dx = 0;
            this.go_back = false;
            this.stop = true;
            this.stopTicks = STOP_TICKS;
        }

        public void setWidth(int width) {
            if (width != this.width) this.reset();
            this.width = width;
        }

        public void setMaxWidth(int maxWidth) {
            // 强制 Unicode 字体时，该宽度经常小范围变动，因此设置容许范围
            if (maxWidth > this.maxWidth + 5 || maxWidth < this.maxWidth - 5) this.reset();
            this.maxWidth = maxWidth;
        }

        public long getRenderTimeDelta() {
            long delta = System.currentTimeMillis() - this.lastRenderTime;
            delta = Math.min(delta, 100);
            this.lastRenderTime = System.currentTimeMillis();
            return delta;
        }

        public void tick(float speed) {
            if (this.width <= this.maxWidth) return;

            float delta = speed * 0.04f * this.getRenderTimeDelta();
            if (this.stop) {
                this.stopTicks -= delta;
                if (this.stopTicks <= 0) {
                    this.stop = false;
                    this.go_back = !this.go_back;
                }
            } else {
                float limit = this.go_back ? 0 : (this.maxWidth - this.width);
                this.dx = this.go_back ? Math.min(limit, this.dx + delta) : Math.max(limit, this.dx - delta);
                if (this.dx == limit) {
                    this.stop = true;
                    this.stopTicks = STOP_TICKS;
                }
            }
        }

        public int getDx() {
           return this.width <= this.maxWidth ? (this.maxWidth - this.width) / 2 : (int) this.dx;
        }
    }

    public static void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Minecraft client = Minecraft.getInstance();
        if (MusicPlayer.INSTANCE.isPlaying()) {

            ClientConfig config = ClientConfig.INSTANCE;
            ClientConfig.ClientConfigOptions options = config.options;
            
            if (!(options.hideWhenChat && client.screen instanceof ChatScreen)) {
                int scaledWidth = client.getWindow().getGuiScaledWidth(), scaledHeight = client.getWindow().getGuiScaledHeight();
                String[] texts = MusicPlayerHandler.INSTANCE.getDisplayTexts();

                if (options.displayLyrics) {
                    Vector2i pos = getPos(config.lyricsPosSupplier, scaledWidth, scaledHeight);
                    RenderUtil.renderText(new TextComponent(texts[0]), options.lyricsAlignment,
                            pos.getX(), pos.getY(), matrices, client.font, (int) config.lyricsColor.getNumber());
                }
                if (options.displaySubLyrics) {
                    Vector2i pos = getPos(config.subLyricsPosSupplier, scaledWidth, scaledHeight);
                    RenderUtil.renderText(new TextComponent(texts[1]), options.subLyricsAlignment,
                            pos.getX(), pos.getY(), matrices, client.font, (int) config.subLyricsColor.getNumber());
                }

                Component text3 = new TextComponent(texts[3]);
                int text3Width = client.font.width(text3);

                if (options.displayMusicDetails) {
                    Vector2i pos = getPos(config.musicDetailsPosSupplier, scaledWidth, scaledHeight);

                    String state = MusicPlayer.INSTANCE.isPlayingTemp ?
                            ConcertoClient.clientState == ConcertoClient.ClientState.MUSIC_AGENT ? " | " + new TranslatableComponent("concerto.agent").getString() :
                            (ConcertoClient.clientState == ConcertoClient.ClientState.MUSIC_ROOM ? " | " + new TranslatableComponent("concerto.room").getString() : "")
                            : "";

                    Component text2 = new TextComponent(texts[2] + state);
                    MUSIC_DETAIL_SCROLL.setMaxWidth(text3Width);
                    MUSIC_DETAIL_SCROLL.setWidth(client.font.width(text2));
                    MUSIC_DETAIL_SCROLL.tick(options.scrollingTextSpeed);

                    int startX = RenderUtil.getTextRenderX(text3, options.musicDetailsAlignment, client.font, pos.getX());
                    RenderUtil.enableScissor(startX, pos.getY(), startX + text3Width, pos.getY() + client.font.lineHeight);
                    RenderUtil.renderText(
                            matrices, client.font, text2, startX + MUSIC_DETAIL_SCROLL.getDx(),
                            pos.getY(), (int) config.musicDetailsColor.getNumber(), options.textShadow
                    );
                    RenderUtil.disableScissor();
                }
                if (options.displayTimeProgress) {
                    Vector2i pos = getPos(config.timeProgressPosSupplier, scaledWidth, scaledHeight);
                    RenderUtil.renderText(text3, options.timeProgressAlignment,
                            pos.getX(), pos.getY(), matrices, client.font, (int) config.timeProgressTextColor.getNumber());
                    int blankWidth = client.font.width("                              "); // 兼容不同字体
                    int timeWidth = (text3Width - blankWidth) / 2;
                    if (MusicPlayerHandler.INSTANCE.currentMeta != null && MusicPlayerHandler.INSTANCE.currentMeta.getDuration() != null) {
                        int x;
                        switch (options.timeProgressAlignment) {
                            case LEFT -> x = pos.getX() + timeWidth + 9;
                            case CENTER -> x = pos.getX() - blankWidth / 2 + 9;
                            default -> x = pos.getX() - blankWidth - timeWidth + 9;
                        }
                        GuiComponent.fill(matrices, x, pos.getY() + 3, x + blankWidth - 20, pos.getY() + 5,
                                (int) config.timeProgressBgColor.getNumber());
                        GuiComponent.fill(matrices, x, pos.getY() + 3, (int) (x + (blankWidth - 20) * MusicPlayerHandler.INSTANCE.progressPercentage),
                                pos.getY() + 5, (int) config.timeProgressColor.getNumber());
                    }
                }

                if (options.displayCoverImg) {
                    matrices.pushPose();

                    Vector2i pos = getPos(config.coverImgPosSupplier, scaledWidth, scaledHeight);
                    int size = config.options.coverImgSize;
                    HEAD_PICTURE.setX(pos.getX());
                    HEAD_PICTURE.setY(pos.getY());
                    HEAD_PICTURE.setSize(size, size);

                    if (options.coverImgRotate) {
                        float cx = pos.getX() + size / 2f;
                        float cy = pos.getY() + size / 2f;
                        float angleRad = delta * (float) Math.PI / 180f;

                        matrices.translate(cx, cy, 0); // 先平移到中心
                        matrices.mulPose(new Quaternion(Vector3f.ZP, angleRad, false)); // 旋转
                        matrices.translate(-cx, -cy, 0); // 再平移回来
                    }

                    HEAD_PICTURE.render(matrices, mouseX, mouseY, delta);
                    matrices.popPose();
                }
            }
        }
    }

    public static Vector2i getPos(ClientConfig.PositionXYSupplier supplier, int scaledWidth, int scaledHeight) {
        return new Vector2i(supplier.getX(scaledWidth), supplier.getY(scaledHeight));
    }
}
