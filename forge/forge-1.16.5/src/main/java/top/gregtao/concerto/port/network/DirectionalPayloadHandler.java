package top.gregtao.concerto.port.network;


import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DirectionalPayloadHandler {

    public static <M> BiConsumer<M, Supplier<NetworkEvent.Context>> createHandler(
            BiConsumer<M, NetworkEvent.Context> clientHandler,
            BiConsumer<M, NetworkEvent.Context> serverHandler
    ) {
        return (payload, contextSupplier) -> {
            NetworkEvent.Context context = contextSupplier.get();
            if (context.getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
                clientHandler.accept(payload, context);
            } else {
                serverHandler.accept(payload, context);
            }
            context.setPacketHandled(true);
        };
    }

}
