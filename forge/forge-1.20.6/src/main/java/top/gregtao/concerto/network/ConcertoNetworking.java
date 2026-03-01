package top.gregtao.concerto.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import top.gregtao.concerto.ConcertoClient;

public class ConcertoNetworking {

    public static final String HANDSHAKE_STRING = "CONCERTO:";

    public static final int WAIT_LIST_MAX_SIZE = 300;

    public static final String VERSION = "1.0.0";

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(ConcertoClient.MOD_ID, "network"))
            .acceptedVersions(Channel.VersionTest.exact(1))
            .optionalServer()
            .networkProtocolVersion(1)
            .simpleChannel();
}
