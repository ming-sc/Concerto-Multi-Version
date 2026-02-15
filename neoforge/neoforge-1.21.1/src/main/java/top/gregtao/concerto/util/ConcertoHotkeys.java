package top.gregtao.concerto.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.screen.ConcertoIndexScreen;
import top.gregtao.concerto.screen.GeneralPlaylistScreen;

public class ConcertoHotkeys {

    public static String CATEGORY = "concerto.hotkey";

    public static KeyMapping GENERAL_PLAYLIST, INDEX_SCREEN, NEXT_MUSIC, PAUSE_RESUME;

    @SuppressWarnings("removal")
    @EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class KeyMappingRegistry {
        @SubscribeEvent
        public static void registerMapping(RegisterKeyMappingsEvent event) {
            GENERAL_PLAYLIST = new KeyMapping(
                    "concerto.hotkey.general_music_list",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_U,
                    CATEGORY
            );
            INDEX_SCREEN = new KeyMapping(
                    "concerto.hotkey.index",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_I,
                    CATEGORY
            );
            NEXT_MUSIC = new KeyMapping(
                    "concerto.screen.next",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_N,
                    CATEGORY
            );
            PAUSE_RESUME = new KeyMapping(
                    "concerto.screen.pause_resume",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_P,
                    CATEGORY
            );

            event.register(GENERAL_PLAYLIST);
            event.register(INDEX_SCREEN);
            event.register(NEXT_MUSIC);
            event.register(PAUSE_RESUME);
        }
    }

    @EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT)
    public static class KeyEventHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (GENERAL_PLAYLIST.consumeClick()) {
                client.setScreen(new GeneralPlaylistScreen(null));
            } else if (INDEX_SCREEN.consumeClick()) {
                client.setScreen(new ConcertoIndexScreen(null));
            } else if (NEXT_MUSIC.consumeClick()) {
                if (!MusicPlayer.INSTANCE.started) MusicPlayer.INSTANCE.start();
                else if (!MusicPlayer.INSTANCE.playNextLock.get()) MusicPlayer.INSTANCE.playNext(1);
            } else if (PAUSE_RESUME.consumeClick()) {
                if (MusicPlayer.INSTANCE.started) {
                    if (MusicPlayer.INSTANCE.forcePaused) MusicPlayer.INSTANCE.forceResume();
                    else MusicPlayer.INSTANCE.forcePause();
                }
            }
        }
    }
}
