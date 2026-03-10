package top.gregtao.concerto.command.builder;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.util.Pair;
import top.gregtao.concerto.port.PlayerUtil;

import java.util.List;
import java.util.function.Supplier;

public class MusicAdderBuilder {

    public static int execute(CommandContext<CommandSourceStack> context,
                              Pair<Music, Component> pair, boolean insert) {
        LocalPlayer player = PlayerUtil.getLocalPlayer();
        Runnable callback = () -> player.displayClientMessage(pair.getSecond(), false);
        if (insert) {
            MusicPlayer.INSTANCE.addMusicHere(pair.getFirst(), true, callback);
        } else {
            MusicPlayer.INSTANCE.addMusic(pair.getFirst(), callback);
        }
        return 0;
    }

    public static int executePlayList(CommandContext<CommandSourceStack> context,
                                      Pair<Supplier<List<Music>>, Component> pair) {
        LocalPlayer player = PlayerUtil.getLocalPlayer();
        MusicPlayer.INSTANCE.addMusic(pair.getFirst(), () -> player.displayClientMessage(pair.getSecond(), false));
        return 0;
    }

    public interface MusicGetter<T> {

        Pair<T, Component> get(CommandContext<CommandSourceStack> context);
    }
}
