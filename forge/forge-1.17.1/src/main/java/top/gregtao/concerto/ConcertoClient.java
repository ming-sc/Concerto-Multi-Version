package top.gregtao.concerto;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.gregtao.concerto.command.MusicCommand;
import top.gregtao.concerto.command.MusicRoomCommand;
import top.gregtao.concerto.command.ShareMusicCommand;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.core.http.kugou.KuGouMusicApiClient;
import top.gregtao.concerto.core.http.netease.NeteaseCloudApiClient;
import top.gregtao.concerto.core.http.qq.QQMusicApiClient;
import top.gregtao.concerto.core.music.list.Playlist;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.player.InitMusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.port.command.forge.RegisterClientCommandsEvent;
import top.gregtao.concerto.screen.InGameHudRenderer;
import top.gregtao.concerto.util.ConcertoHotkeys;
import top.gregtao.concerto.core.util.ConcertoRunner;

import java.util.List;

public class ConcertoClient {

	public static final String MOD_ID = "concerto";

	public static final Logger LOGGER = LogManager.getLogger("ConcertoClient");

	// ======================================================
	// Server States

	public static ClientState clientState = ClientState.LOCAL;

	public static boolean serverAvailable = false;

	public static List<Playlist> presetRadios = List.of();

	public static boolean isServerAvailable() {
		return serverAvailable || !ClientConfig.INSTANCE.options.handshakeRequired ||
				Minecraft.getInstance().isLocalServer();
	}

	public enum ClientState {
		LOCAL,
		MUSIC_ROOM,
		MUSIC_AGENT
	}

	// ======================================================

	public static void concertoClientInit(IEventBus modEventBus) {
		InitMusicPlayer.init();
		InGameHudRenderer.init();
		modEventBus.addListener(ClientMusicNetworkHandler::register);
	}

	@Mod.EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT)
	public static class ClientCommandRegistry {
		/**
		 * 注意这里的 {@link RegisterClientCommandsEvent} 不是 forge 的
		 */
		@SubscribeEvent
		public static void onClientCommandRegister(RegisterClientCommandsEvent event) {
			CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
			MusicCommand.register(dispatcher);
			ShareMusicCommand.register(dispatcher);
			MusicRoomCommand.register(dispatcher);
		}
	}

	@Mod.EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ClientReloadListenerRegistry {
		@SubscribeEvent
		public static void onClientAddReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(
					(ResourceManagerReloadListener) resourceManager -> {
						ConcertoRunner.run(() -> {
							ClientConfig.INSTANCE.readOptions();
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

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			event.enqueueWork(ConcertoHotkeys.KeyMappingRegistry::registerMapping);
		}
	}
}
