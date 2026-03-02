package top.gregtao.concerto.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.screen.ConcertoIndexScreen;
import top.gregtao.concerto.screen.GeneralPlaylistScreen;

public class ConcertoHotkeys {

    public static String CATEGORY = "concerto.hotkey";

    public static KeyMapping GENERAL_PLAYLIST, INDEX_SCREEN, NEXT_MUSIC, PAUSE_RESUME;

    public static class KeyMappingRegistry {
        public static void registerMapping() {
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

            ClientRegistry.registerKeyBinding(GENERAL_PLAYLIST);
            ClientRegistry.registerKeyBinding(INDEX_SCREEN);
            ClientRegistry.registerKeyBinding(NEXT_MUSIC);
            ClientRegistry.registerKeyBinding(PAUSE_RESUME);
        }
    }

    @Mod.EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT)
    public static class KeyEventHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
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
