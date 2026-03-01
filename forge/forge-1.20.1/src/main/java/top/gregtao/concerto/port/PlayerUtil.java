package top.gregtao.concerto.port;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class PlayerUtil {

    public static LocalPlayer getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

}
