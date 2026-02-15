package top.gregtao.concerto.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.core.bridge.IComponent;
import top.gregtao.concerto.port.PlayerUtil;

public class ComponentUtil implements IComponent {

    @Override
    public String getTranslatable(String key) {
        return Component.translatable(key).getString();
    }

    @Override
    public void displayClientMessage(boolean actionBar, String key, Object... args) {
        LocalPlayer player = PlayerUtil.getLocalPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable(key, args), actionBar);
        }
    }
}
