package top.gregtao.concerto.screen.login;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.TextWidget;
import top.gregtao.concerto.util.ButtonBuilder;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PasswordLoginScreen extends ConcertoScreen {
    private TextFieldWidget usernameField, passwordField;
    private boolean showPassword = true;
    private final BiFunction<String, String, ITextComponent> loginHandler;
    private final Supplier<Boolean> loginChecker;

    public PasswordLoginScreen(Supplier<Boolean> loginChecker, BiFunction<String, String, ITextComponent> loginHandler, ITextComponent title, Screen parent) {
        super(new StringTextComponent(new TranslationTextComponent("concerto.screen.login").getString() + title.getString()), parent);
        this.loginChecker = loginChecker;
        this.loginHandler = loginHandler;
    }

    @Override
    protected void init() {
        super.init();
        this.usernameField = new TextFieldWidget(this.font, this.width / 2 - 30, 20, 155, 20, StringTextComponent.EMPTY);
        this.addButton(this.usernameField);
        TextWidget textWidget = new TextWidget(this.width / 2 - 120, 22, 90, 20, new TranslationTextComponent("concerto.screen.login.username"), this.font);
        textWidget.alignLeft();
        this.addButton(textWidget);

        this.passwordField = new TextFieldWidget(this.font, this.width / 2 - 30, 50, 90, 20, StringTextComponent.EMPTY);
        this.addButton(this.passwordField);
        TextWidget textWidget1 = new TextWidget(this.width / 2 - 120, 52, 90, 20, new TranslationTextComponent("concerto.screen.login.password"), this.font);
        textWidget1.alignLeft();
        this.addButton(textWidget1);
        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.login.show_password"), button -> this.switchShowPassword())
                .pos(this.width / 2 + 65, 50).size(60, 20).build());

        this.addButton(new ButtonBuilder(new TranslationTextComponent("concerto.screen.login.confirm"), button -> this.tryLogin())
                .pos(this.width / 2 - 32, 80).size(157, 20).build());

        this.switchShowPassword();
    }

    public void switchShowPassword() {
        this.showPassword = !this.showPassword;
        this.passwordField.setFormatter(!this.showPassword ?
                (s, f) -> IReorderingProcessor.forward("*".repeat(s.length()), Style.EMPTY) :
                (s, f) -> IReorderingProcessor.forward(s, Style.EMPTY)
        );
    }

    public void tryLogin() {
        String username = this.usernameField.getValue().trim(), password = this.passwordField.getValue().trim();
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
    }
}
