package top.gregtao.concerto.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.network.ServerMusicNetworkHandler;
import top.gregtao.concerto.network.room.MusicRoom;
import top.gregtao.concerto.network.room.ServerMusicAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(at = @At("TAIL"), method = "placeNewPlayer")
    public void onPlayerConnectInject(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        Executor delayedExecutor = CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS);
        CompletableFuture.runAsync(() -> ServerMusicNetworkHandler.playerJoinHandshake(player), delayedExecutor);
    }

    @Inject(at = @At("HEAD"), method = "remove")
    public void removeInject(ServerPlayer player, CallbackInfo ci) {
        List<UUID> removeList = new ArrayList<>();
        for (Map.Entry<UUID, MusicRoom> entry : MusicRoom.ROOMS.entrySet()) {
            if (entry.getValue().owner.equals(player.getName().getString())) {
                removeList.add(entry.getKey());
                try {
                    entry.getValue().serverOnRemove(player.getName().getString(), player.level().getServer());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            if (entry.getValue().members.containsKey(player.getName().getString())) {
                entry.getValue().serverOnQuit(player.getName().getString(), player.level().getServer());
                break;
            }
        }
        removeList.forEach(MusicRoom.ROOMS::remove);
        if (ServerMusicAgent.INSTANCE.isMember(player)) ServerMusicAgent.INSTANCE.playerQuit(player);
    }
}
