package top.gregtao.concerto.command.builder;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.util.Pair;
import top.gregtao.concerto.port.PlayerUtil;

import java.util.List;
import java.util.function.Supplier;

public class MusicAdderBuilder {

    public static int execute(CommandContext<CommandSource> context,
                              Pair<Music, ITextComponent> pair, boolean insert) {
        ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
        Runnable callback = () -> player.displayClientMessage(pair.getSecond(), false);
        if (insert) {
            MusicPlayer.INSTANCE.addMusicHere(pair.getFirst(), true, callback);
        } else {
            MusicPlayer.INSTANCE.addMusic(pair.getFirst(), callback);
        }
        return 0;
    }

    public static int executePlayList(CommandContext<CommandSource> context,
                                      Pair<Supplier<List<Music>>, ITextComponent> pair) {
        ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
        MusicPlayer.INSTANCE.addMusic(pair.getFirst(), () -> player.displayClientMessage(pair.getSecond(), false));
        return 0;
    }

    public interface MusicGetter<T> {

        Pair<T, ITextComponent> get(CommandContext<CommandSource> context);
    }
}
