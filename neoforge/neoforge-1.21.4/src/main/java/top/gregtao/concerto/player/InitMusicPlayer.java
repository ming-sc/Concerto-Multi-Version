package top.gregtao.concerto.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
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
            Options options = client.options;
            return options.getSoundSourceVolume(SoundSource.MASTER) * options.getSoundSourceVolume(SoundSource.MUSIC) * 0.5;
        };
    }

}
