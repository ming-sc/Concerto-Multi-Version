package top.gregtao.concerto.port;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;

public class PlayerUtil {

    public static ClientPlayerEntity getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

}
