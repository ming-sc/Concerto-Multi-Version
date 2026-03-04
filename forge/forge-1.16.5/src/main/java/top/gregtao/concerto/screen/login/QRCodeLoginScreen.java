package top.gregtao.concerto.screen.login;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.util.ButtonBuilder;
import top.gregtao.concerto.util.RenderUtil;

import java.util.function.Function;
import java.util.function.Supplier;

public class QRCodeLoginScreen extends ConcertoScreen {
    private final Supplier<String> qrKeySupplier;
    private final Function<String, Status> statusUpdater;
    private final Function<String, byte[]> imageUpdater;
    private String key;
    private Status status = Status.EMPTY;
    private int timer = 0;
    private final int qrWidth;
    private final int qrHeight;
    private ITextComponent message = StringTextComponent.EMPTY;
    private boolean updaterLock = false;
    private URLImageWidget urlImageWidget;

    public QRCodeLoginScreen(Supplier<String> qrKeySupplier, Function<String, byte[]> imageUpdater,
                             Function<String, Status> statusUpdater, int width, int height, ITextComponent title, Screen parent) {
        super(new StringTextComponent(new TranslationTextComponent("concerto.screen.login").getString() + title.getString()), parent);
        this.qrKeySupplier = qrKeySupplier;
        this.statusUpdater = statusUpdater;
        this.imageUpdater = imageUpdater;
        this.qrWidth = width;
        this.qrHeight = height;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.login.qrcode.refresh"), button -> {
            this.timer = 0;
            this.status = Status.EMPTY;
        }).size(100, 20).pos(this.width / 2 - 50, this.height - 40).build());
        this.urlImageWidget = new URLImageWidget(this.qrWidth, this.qrHeight, this.width / 2 - this.qrWidth / 2, 30, null);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.timer == 0) {
            switch (this.status) {
                case EMPTY -> this.loadQRCode();
                case FAILED -> this.loadQRCode(new TranslationTextComponent("concerto.screen.login.qrcode.failed"));
                case EXPIRED -> this.loadQRCode(new TranslationTextComponent("concerto.screen.login.qrcode.expired"));
                case SUCCESS -> {
                    ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
                    if (player != null) {
                        player.displayClientMessage(new TranslationTextComponent("concerto.screen.login.qrcode.success"), false);
                    }
                    Minecraft.getInstance().setScreen(null);
                }
                case WAITING -> {
                    if (!this.updaterLock) {
                        ConcertoRunner.run(() -> {
                            this.updaterLock = true;
                            this.status = this.statusUpdater.apply(this.key);
                            this.updaterLock = false;
                        });
                    }
                }
            }
        }
        this.timer = (this.timer + 1) % 40;
    }

    public void loadQRCode() {
        ConcertoRunner.run(() -> {
            String link = this.key = this.qrKeySupplier.get();
            this.urlImageWidget.setUrl(link);
            if (this.imageUpdater != null) this.urlImageWidget.loadImage(this.imageUpdater, false);
            else this.urlImageWidget.loadImage();
            this.status = Status.WAITING;
        });
    }

    public void loadQRCode(ITextComponent msg) {
        this.message = msg;
        this.loadQRCode();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.urlImageWidget.render(matrices, mouseX, mouseY, delta);
        RenderUtil.renderCenteredString(matrices, this.font, this.message, this.width / 2, 120, 0xffffffff);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.urlImageWidget.close();
    }

    public enum Status {
        EMPTY,
        WAITING,
        EXPIRED,
        SUCCESS,
        FAILED
    }
}
