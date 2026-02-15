package top.gregtao.concerto;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.gregtao.concerto.command.ConcertoServerCommand;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.core.config.ServerConfig;
import top.gregtao.concerto.core.ConcertoCore;
import top.gregtao.concerto.core.music.list.Playlist;
import top.gregtao.concerto.core.http.kugou.KuGouMusicApiClient;
import top.gregtao.concerto.core.http.netease.NeteaseCloudApiClient;
import top.gregtao.concerto.core.http.qq.QQMusicApiClient;
import top.gregtao.concerto.network.ServerMusicNetworkHandler;
import top.gregtao.concerto.network.room.ServerMusicAgent;
import top.gregtao.concerto.util.ComponentUtil;

@Mod(value = ConcertoClient.MOD_ID)
public class ConcertoServer {

    public static Logger LOGGER = LoggerFactory.getLogger("ConcertoServer");

    public ConcertoServer(IEventBus modEventBus, ModContainer modContainer) {
        ConcertoCore.init(new ComponentUtil());
        // 防止客户端重复注册
        if (FMLLoader.getDist() == Dist.DEDICATED_SERVER) {
            modEventBus.addListener(ServerMusicNetworkHandler::register);
        }
    }

    public static void reload() {
        ServerConfig.INSTANCE.readOptions();
        if (FMLLoader.getDist() == Dist.DEDICATED_SERVER) {
            // 只在专用服务器同步配置
            ClientConfig.INSTANCE.options.kuGouMusicLite = ServerConfig.INSTANCE.options.kuGouMusicLite;
        }
        PresetPlaylistsConfig.PRESET_RADIOS.read();
        PresetPlaylistsConfig.SERVER_FREE_TIME_PLAYLIST.stream()
                .map(Playlist::getList)
                .forEach(ServerMusicAgent.INSTANCE.freeTimePlaylist::addAll);
        NeteaseCloudApiClient.INSTANCE.readCookie();
        QQMusicApiClient.INSTANCE.readCookie();
        KuGouMusicApiClient.INSTANCE.readCookie();
    }

    @EventBusSubscriber(modid = ConcertoClient.MOD_ID)
    public static class ServerEvent {
        @SubscribeEvent
        public static void onServerCommandRegister(RegisterCommandsEvent event) {
            ConcertoServerCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onServerAddReloadListeners(AddReloadListenerEvent event) {
            event.addListener((ResourceManagerReloadListener) resourceManager -> reload());
        }
    }
}
