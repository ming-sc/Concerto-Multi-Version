package top.gregtao.concerto.port.network;

import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.BiConsumer;

public class DirectionalPayloadHandler {

    public static <M> BiConsumer<M, CustomPayloadEvent.Context> createHandler(
            BiConsumer<M, CustomPayloadEvent.Context> clientHandler,
            BiConsumer<M, CustomPayloadEvent.Context> serverHandler
    ) {
        return (payload, context) -> {
            if (context.isClientSide()) {
                clientHandler.accept(payload, context);
            } else {
                serverHandler.accept(payload, context);
            }
            context.setPacketHandled(true);
        };
    }

}
