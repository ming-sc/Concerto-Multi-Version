package top.gregtao.concerto.util;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.core.bridge.IComponent;
import top.gregtao.concerto.port.PlayerUtil;

public class ComponentUtil implements IComponent {

    @Override
    public String getTranslatable(String key) {
        return new TranslationTextComponent(key).getString();
    }

    @Override
    public void displayClientMessage(boolean actionBar, String key, Object... args) {
        ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
        if (player != null) {
            player.displayClientMessage(new TranslationTextComponent(key, args), actionBar);
        }
    }
}
