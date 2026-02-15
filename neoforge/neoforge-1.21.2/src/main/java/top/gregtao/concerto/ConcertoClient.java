package top.gregtao.concerto;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.gregtao.concerto.command.MusicCommand;
import top.gregtao.concerto.command.MusicRoomCommand;
import top.gregtao.concerto.command.ShareMusicCommand;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.core.http.kugou.KuGouMusicApiClient;
import top.gregtao.concerto.core.http.netease.NeteaseCloudApiClient;
import top.gregtao.concerto.core.http.qq.QQMusicApiClient;
import top.gregtao.concerto.core.music.list.Playlist;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.player.InitMusicPlayer;
import top.gregtao.concerto.screen.InGameHudRenderer;
import top.gregtao.concerto.util.ConcertoOptions;

import java.util.List;

@Mod(value = ConcertoClient.MOD_ID, dist = Dist.CLIENT)
public class ConcertoClient {

	public static final String MOD_ID = "concerto";

	public static final Logger LOGGER = LoggerFactory.getLogger("ConcertoClient");

	// ======================================================
	// Server States

	public static ClientState clientState = ClientState.LOCAL;

	public static boolean serverAvailable = false;

	public static List<Playlist> presetRadios = List.of();

	public static boolean isServerAvailable() {
		return serverAvailable || !ClientConfig.INSTANCE.options.handshakeRequired ||
				Minecraft.getInstance().isSingleplayer();
	}

	public enum ClientState {
		LOCAL,
		MUSIC_ROOM,
		MUSIC_AGENT
	}

	// ======================================================

	public ConcertoClient(IEventBus modEventBus, ModContainer modContainer) {
		InitMusicPlayer.init();
		InGameHudRenderer.init();
		modEventBus.addListener(ClientMusicNetworkHandler::register);
	}

	@EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT)
	public static class ClientCommandRegistry {
		@SubscribeEvent
		public static void onClientCommandRegister(RegisterClientCommandsEvent event) {
			CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
			MusicCommand.register(dispatcher);
			ShareMusicCommand.register(dispatcher);
			MusicRoomCommand.register(dispatcher);
		}

	}

	@SuppressWarnings("removal")
	@EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static class ClientReloadListenerRegistry {
		@SubscribeEvent
		public static void onClientAddReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(
					(ResourceManagerReloadListener) resourceManager -> {
						ConcertoRunner.run(() -> {
							ClientConfig.INSTANCE.readOptions();
							ConcertoOptions.INSTANCE.readOptions();
							MusicPlayer.INSTANCE.reloadConfig(() -> LOGGER.info("Loaded general music playlist"));
							PresetPlaylistsConfig.LOCAL_PLAYLISTS.read();
							NeteaseCloudApiClient.LOCAL_USER.updateLoginStatus();
							QQMusicApiClient.LOCAL_USER.updateLoginStatus();

							// 酷狗音乐相关
							KuGouMusicApiClient.LOCAL_USER.updateLoginStatusAndDfid();
							// 刷新 token, 延长 token 有效时间
							KuGouMusicApiClient.INSTANCE.refreshToken();
							// 更新 VIP 状态
							KuGouMusicApiClient.LOCAL_USER.updateVIPStatus();
							// 自动获取每日酷狗音乐 VIP
							if (ClientConfig.INSTANCE.options.kuGouMusicLite &&
									KuGouMusicApiClient.LOCAL_USER.isLoggedIn() &&
									ClientConfig.INSTANCE.options.autoGetKuGouDailyVIP) {
								KuGouMusicApiClient.INSTANCE.receiveVip();
							}
						});
					}
			);
		}
	}
}
