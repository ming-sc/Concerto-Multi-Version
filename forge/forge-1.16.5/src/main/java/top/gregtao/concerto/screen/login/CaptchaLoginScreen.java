package top.gregtao.concerto.screen.login;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.TextWidget;
import top.gregtao.concerto.util.ButtonBuilder;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CaptchaLoginScreen extends ConcertoScreen {
    private TextFieldWidget usernameField, captchaField;
    private Button captchaButton;
    private int captchaTimer = -1;
    private final Consumer<String> callForCaptcha;
    private final BiFunction<String, String, ITextComponent> loginHandler;
    private final Supplier<Boolean> loginChecker;

    public CaptchaLoginScreen(Consumer<String> callForCaptcha, Supplier<Boolean> loginChecker,
                              BiFunction<String, String, ITextComponent> loginHandler, ITextComponent title, Screen parent) {
        super(new StringTextComponent(new TranslationTextComponent("concerto.screen.login").getString() + title.getString()), parent);
        this.callForCaptcha = callForCaptcha;
        this.loginChecker = loginChecker;
        this.loginHandler = loginHandler;
    }

    @Override
    protected void init() {
        super.init();
        this.usernameField = new TextFieldWidget(this.font, this.width / 2 - 30, 20, 90, 20, StringTextComponent.EMPTY);
        this.addButton(this.usernameField);
        TextWidget textWidget = new TextWidget(this.width / 2 - 120, 22, 90, 20, new TranslationTextComponent("concerto.screen.login.username"), this.font);
        textWidget.alignLeft();
        this.addButton(textWidget);
        this.captchaButton = new ButtonBuilder(new TranslationTextComponent("concerto.screen.login.get_captcha"), button -> {
            if (this.usernameField.getValue().isEmpty()) {
                this.displayAlert(new TranslationTextComponent("concerto.screen.login.empty"));
            } else {
                this.captchaButton.active = false;
                this.captchaTimer = 400;
                this.callForCaptcha.accept(this.usernameField.getValue());
            }
        }).pos(this.width / 2 + 65, 20).size(60, 20).build();
        this.addButton(this.captchaButton);

        this.captchaField = new TextFieldWidget(this.font, this.width / 2 - 30, 50, 155, 20, StringTextComponent.EMPTY);
        this.addButton(this.captchaField);
        TextWidget textWidget1 = new TextWidget(this.width / 2 - 120, 52, 90, 20, new TranslationTextComponent("concerto.screen.login.captcha"), this.font);
        textWidget1.alignLeft();
        this.addButton(textWidget1);

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.login.confirm"), button -> this.tryLogin())
                .pos(this.width / 2 - 32, 80).size(157, 20).build());
    }

    public void tryLogin() {
        String username = this.usernameField.getValue().trim(), password = this.captchaField.getValue().trim();
        if (username.isEmpty() || password.isEmpty()) {
            this.displayAlert(new TranslationTextComponent("concerto.screen.login.empty"));
        } else {
            this.displayAlert(this.loginHandler.apply(username, password));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.loginChecker.get()) {
            ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
            if (player != null) {
                player.displayClientMessage(new TranslationTextComponent("concerto.screen.login.success"), false);
            }
            Minecraft.getInstance().setScreen(null);
        }
        if (this.captchaTimer > 0 && --this.captchaTimer == 0) {
            this.captchaButton.active = true;
            this.captchaTimer = -1;
        }
    }
}
