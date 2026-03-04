package top.gregtao.concerto.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class NetworkHelper {

    public static void sendToServer(Object object) {
        ConcertoNetworking.CHANNEL.send(PacketDistributor.SERVER.noArg(), object);
    }

    public static void sendToPlayer(ServerPlayer player, Object object) {
        ConcertoNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), object);
    }
}
