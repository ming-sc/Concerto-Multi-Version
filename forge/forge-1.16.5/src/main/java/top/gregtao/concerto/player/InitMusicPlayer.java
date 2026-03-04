package top.gregtao.concerto.player;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.network.room.MusicRoom;

public class InitMusicPlayer {

    public static void init() {
        MusicPlayer player = MusicPlayer.INSTANCE;
        player.onPlay = () -> {
            Minecraft client = Minecraft.getInstance();
            client.getMusicManager().stopPlaying();
        };
        player.onForceResume = () -> MusicRoom.clientPause(false);
        player.onPause = () -> MusicRoom.clientPause(true);
        player.onResume = () -> MusicRoom.clientPause(false);
        player.onPlayNext = MusicRoom::clientUpdate;
        player.volumeSupplier = () -> {
            Minecraft client = Minecraft.getInstance();
            GameSettings options = client.options;
            return options.getSoundSourceVolume(SoundCategory.MASTER) * options.getSoundSourceVolume(SoundCategory.MUSIC) * 0.5;
        };
    }

}
