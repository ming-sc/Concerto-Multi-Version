package top.gregtao.concerto.screen.login;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.screen.ConcertoScreen;
import top.gregtao.concerto.screen.widget.TextWidget;
import top.gregtao.concerto.util.ButtonBuilder;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PasswordLoginScreen extends ConcertoScreen {
    private EditBox usernameField, passwordField;
    private boolean showPassword = true;
    private final BiFunction<String, String, Component> loginHandler;
    private final Supplier<Boolean> loginChecker;

    public PasswordLoginScreen(Supplier<Boolean> loginChecker, BiFunction<String, String, Component> loginHandler, Component title, Screen parent) {
        super(new TextComponent(new TranslatableComponent("concerto.screen.login").getString() + title.getString()), parent);
        this.loginChecker = loginChecker;
        this.loginHandler = loginHandler;
    }

    @Override
    protected void init() {
        super.init();
        this.usernameField = new EditBox(this.font, this.width / 2 - 30, 20, 155, 20, TextComponent.EMPTY);
        this.addWidget(this.usernameField);
        this.addRenderableWidget(this.usernameField);
        TextWidget textWidget = new TextWidget(this.width / 2 - 120, 22, 90, 20, new TranslatableComponent("concerto.screen.login.username"), this.font);
        textWidget.alignLeft();
        this.addRenderableWidget(textWidget);

        this.passwordField = new EditBox(this.font, this.width / 2 - 30, 50, 90, 20, TextComponent.EMPTY);
        this.addWidget(this.passwordField);
        this.addRenderableWidget(this.passwordField);
        TextWidget textWidget1 = new TextWidget(this.width / 2 - 120, 52, 90, 20, new TranslatableComponent("concerto.screen.login.password"), this.font);
        textWidget1.alignLeft();
        this.addRenderableWidget(textWidget1);
        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.login.show_password"), button -> this.switchShowPassword())
                .pos(this.width / 2 + 65, 50).size(60, 20).build());

        this.addRenderableWidget(new ButtonBuilder(new TranslatableComponent("concerto.screen.login.confirm"), button -> this.tryLogin())
                .pos(this.width / 2 - 32, 80).size(157, 20).build());

        this.switchShowPassword();
    }

    public void switchShowPassword() {
        this.showPassword = !this.showPassword;
        this.passwordField.setFormatter(!this.showPassword ?
                (s, f) -> FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY) :
                (s, f) -> FormattedCharSequence.forward(s, Style.EMPTY)
        );
    }

    public void tryLogin() {
        String username = this.usernameField.getValue().trim(), password = this.passwordField.getValue().trim();
        if (username.isEmpty() || password.isEmpty()) {
            this.displayAlert(new TranslatableComponent("concerto.screen.login.empty"));
        } else {
            this.displayAlert(this.loginHandler.apply(username, password));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.loginChecker.get()) {
            LocalPlayer player = PlayerUtil.getLocalPlayer();
            if (player != null) {
                player.displayClientMessage(new TranslatableComponent("concerto.screen.login.success"), false);
            }
            Minecraft.getInstance().setScreen(null);
        }
    }
}
