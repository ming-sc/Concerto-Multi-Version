package top.gregtao.concerto.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class NetworkHelper {

    public static void sendToServer(Object object) {
        ConcertoNetworking.CHANNEL.send(PacketDistributor.SERVER.noArg(), object);
    }

    public static void sendToPlayer(ServerPlayerEntity player, Object object) {
        ConcertoNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), object);
    }
}
