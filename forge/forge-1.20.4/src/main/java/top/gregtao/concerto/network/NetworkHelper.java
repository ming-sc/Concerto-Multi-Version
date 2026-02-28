package top.gregtao.concerto.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class NetworkHelper {

    public static void sendToServer(Object object) {
        ConcertoNetworking.CHANNEL.send(object, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(ServerPlayer player, Object object) {
        ConcertoNetworking.CHANNEL.send(object, PacketDistributor.PLAYER.with(player));
    }
}
