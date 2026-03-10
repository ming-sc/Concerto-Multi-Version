package top.gregtao.concerto.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.command.argument.OrderTypeArgumentType;
import top.gregtao.concerto.command.builder.MusicAdderBuilder;
import top.gregtao.concerto.core.api.CacheableMusic;
import top.gregtao.concerto.core.api.Likeable;
import top.gregtao.concerto.core.config.CacheManager;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.config.MusicCacheManager;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.core.enums.OrderType;
import top.gregtao.concerto.core.enums.Sources;
import top.gregtao.concerto.core.music.HttpFileMusic;
import top.gregtao.concerto.core.music.LocalFileMusic;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.music.list.FixedPlaylist;
import top.gregtao.concerto.core.music.meta.music.MusicMetaData;
import top.gregtao.concerto.core.music.meta.music.list.PlaylistMetaData;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.core.util.Pair;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.port.command.ClientCommandManager;
import top.gregtao.concerto.util.CommandUtil;
import top.gregtao.concerto.util.RenderUtil;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MusicCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(registerPlayerControllers(
                ClientCommandManager.literal("concerto")
                        .then(addMusicCommand())
                        .then(insertMusicCommand())
        ));
        if (ClientConfig.INSTANCE.options.registerMusicCommand) {
            dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("music").redirect(node));
        }
    }

    private static final List<MusicAdderBuilder.MusicGetter<Music>> GETTERS = List.of(
            context -> {
                LocalFileMusic music = new LocalFileMusic(StringArgumentType.getString(context, "path"));
                return Pair.of(music, Component.translatable(Sources.LOCAL_FILE.getKey("add"), music.getRawPath()));
            },
            context -> {
                HttpFileMusic music = new HttpFileMusic(StringArgumentType.getString(context, "path"));
                return Pair.of(music, Component.translatable(Sources.INTERNET.getKey("add"), music.getRawPath()));
            }
    );

    public static LiteralArgumentBuilder<CommandSourceStack> registerPlayerControllers(
            LiteralArgumentBuilder<CommandSourceStack> builder) {
        MusicPlayer player = MusicPlayer.INSTANCE;
        return builder.then(
                ClientCommandManager.literal("pause").executes(context -> {
                    if (player.forcePaused) {
                        player.forceResume();
                        CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.resume"));
                    } else {
                        player.forcePause();
                        CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.pause"));
                    }
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("start").executes(context -> {
                    if (!player.started) {
                        player.start();
                        CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.start"));
                    } else {
                        CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.already_started"));
                    }
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("stop").executes(context -> {
                    player.started = false;
                    player.playNextLock.set(true);
                    player.stop();
                    MusicPlayerHandler.INSTANCE.resetInfo();
                    CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.stop"));
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("skip").executes(context -> {
                    MusicPlayer.INSTANCE.stop();
                    CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.skip"));
                    return 0;
                }).then(
                        ClientCommandManager.argument("index", IntegerArgumentType.integer(1)).executes(context -> {
                            int index = IntegerArgumentType.getInteger(context, "index");
                            MusicPlayer.INSTANCE.skipTo(index - 1);
                            CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.skip_to", index));
                            return 0;
                        })
                )
        ).then(
                ClientCommandManager.literal("cut").executes(context -> {
                    MusicPlayer.INSTANCE.cut(() -> {});
                    CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.cut"));
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("clear").executes(context -> {
                    MusicPlayer.INSTANCE.clear();
                    MusicPlayer.resetInstance();
                    CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.clear"));
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("restart").executes(context -> {
                    MusicPlayer.resetInstance();
                    CommandUtil.commandMessageClient(context, Component.translatable("concerto.success"));
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("mode").then(
                        ClientCommandManager.argument("mode", OrderTypeArgumentType.orderType()).executes((context -> {
                            OrderType type = OrderTypeArgumentType.getOrderType(context, "mode");
                            MusicPlayerHandler.INSTANCE.setOrderType(type);
                            CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.mode", type.getI18nString()));
                            return 0;
                        }))
                )
        ).then(
                ClientCommandManager.literal("reload").executes(context -> {
                    MusicPlayer.INSTANCE.reloadConfig(() ->
                            CommandUtil.commandMessageClient(context, Component.translatable("concerto.player.reload")));
                    ClientConfig.INSTANCE.readOptions();
                    PresetPlaylistsConfig.LOCAL_PLAYLISTS.read();
                    MusicPlayer.resetInstance();
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("list").then(
                        ClientCommandManager.argument("page", IntegerArgumentType.integer(1)).executes(context -> {
                            LocalPlayer clientPlayer = PlayerUtil.getLocalPlayer();
                            ConcertoRunner.run(() -> {
                                int page = IntegerArgumentType.getInteger(context, "page");
                                List<Music> list = MusicPlayerHandler.INSTANCE.getMusicList();
                                page = Math.min(page, (int) Math.ceil(list.size() / 10f));
                                clientPlayer.displayClientMessage(CommandUtil.PAGE_SPLIT, false);
                                for (int i = 10 * (page - 1); i < Math.min(10 * page, list.size()); ++i) {
                                    MusicMetaData meta = list.get(i).getMeta();
                                    clientPlayer.displayClientMessage(Component.literal(
                                                    (i + 1) + ". " + meta.title() + " | " + meta.author()
                                                            + " | " + meta.getSource() + " | " + meta.getDuration().toShortString())
                                            .setStyle(RenderUtil.getRunCommandStyle("/concerto skip " + (i + 1))), false);
                                }
                                clientPlayer.displayClientMessage(CommandUtil.PAGE_SPLIT, false);
                            });
                            return 0;
                        })
                )
        ).then(
                ClientCommandManager.literal("save").executes(context -> {
                    LocalPlayer clientPlayer = PlayerUtil.getLocalPlayer();
                    if (MusicPlayerHandler.INSTANCE.currentMusic == null) {
                        clientPlayer.displayClientMessage(Component.translatable("concerto.unknown"), false);
                    } else if (MusicPlayerHandler.INSTANCE.currentMusic instanceof CacheableMusic music) {
                        ConcertoRunner.run(() -> {
                            try {
                                MusicCacheManager.INSTANCE.addMusic(music);
                                clientPlayer.displayClientMessage(Component.translatable("concerto.success"), false);
                            } catch (IOException | UnsupportedAudioFileException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        clientPlayer.displayClientMessage(Component.translatable("concerto.not_cacheable"), false);
                    }
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("like").executes(context -> {
                    LocalPlayer clientPlayer = PlayerUtil.getLocalPlayer();
                    Music music = MusicPlayerHandler.INSTANCE.getCurrentMusic();
                    if (music instanceof Likeable likeable) {
                        CompletableFuture.supplyAsync(likeable::likeIt, ConcertoRunner.RUNNERS_POOL).thenAcceptAsync(success ->
                                clientPlayer.displayClientMessage(success ? Component.translatable("concerto.like",
                                        music.getMeta().title(), music.getMeta().getSource()) :
                                        Component.translatable("concerto.fail"), false), ConcertoRunner.RUNNERS_POOL);
                    } else {
                        clientPlayer.displayClientMessage(Component.translatable("concerto.error.unsupported_operation"), false);
                    }
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("dislike").executes(context -> {
                    LocalPlayer clientPlayer = PlayerUtil.getLocalPlayer();
                    Music music = MusicPlayerHandler.INSTANCE.getCurrentMusic();
                    if (music instanceof Likeable likeable) {
                        CompletableFuture.supplyAsync(likeable::dislikeIt, ConcertoRunner.RUNNERS_POOL).thenAcceptAsync(success ->
                                clientPlayer.displayClientMessage(success ? Component.translatable("concerto.dislike",
                                        music.getMeta().title(), music.getMeta().getSource()) :
                                        Component.translatable("concerto.fail"), false), ConcertoRunner.RUNNERS_POOL);
                    } else {
                        clientPlayer.displayClientMessage(Component.translatable("concerto.error.unsupported_operation"), false);
                    }
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("download-current").executes(context -> {
                    MusicPlayerHandler.downloadMusics(List.of(MusicPlayerHandler.INSTANCE.getCurrentMusic()));
                    PlayerUtil.getLocalPlayer().displayClientMessage(Component.translatable("concerto.success"), false);
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("download-all").executes(context -> {
                    MusicPlayerHandler.downloadMusics(MusicPlayerHandler.INSTANCE.getMusicList());
                    PlayerUtil.getLocalPlayer().displayClientMessage(Component.translatable("concerto.success"), false);
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("export-as-playlist").executes(context -> {
                    LocalPlayer clientPlayer = PlayerUtil.getLocalPlayer();
                    Component playerName = clientPlayer.getDisplayName();
                    if (PresetPlaylistsConfig.saveToLocalPlaylists(new FixedPlaylist(
                            MusicPlayerHandler.INSTANCE.getMusicList(),
                            new PlaylistMetaData(
                                    playerName == null ? "Unknown" : playerName.getString(),
                                    "Default Playlist",
                                    LocalDateTime.now().toString(),
                                    "Default Playlist"
                            ),
                            false
                    ))) {
                        clientPlayer.displayClientMessage(Component.translatable("concerto.playlist.export.success"), false);
                    } else {
                        clientPlayer.displayClientMessage(Component.translatable("concerto.playlist.export.fail"), false);
                    }
                    return 0;
                })
        ).then(
                ClientCommandManager.literal("clean-cache").executes(context -> {
                    CacheManager.cleanAllCache();
                    return 0;
                })
        );
    }

    public static ArgumentBuilder<CommandSourceStack, ?> addMusicCommand() {
        return ClientCommandManager.literal("add").then(
                ClientCommandManager.literal("local").then(
                        ClientCommandManager.argument("path", StringArgumentType.string()).executes(
                                context -> MusicAdderBuilder.execute(context, GETTERS.get(0).get(context), false)
                        )
                ).then(
                        ClientCommandManager.literal("folder").then(
                                ClientCommandManager.argument("path", StringArgumentType.string()).executes(context -> {
                                    String path = StringArgumentType.getString(context, "path");
                                    MusicPlayer.INSTANCE.addMusic(
                                            () -> LocalFileMusic.getMusicsInFolder(new File(path)),
                                            () -> PlayerUtil.getLocalPlayer().displayClientMessage(
                                                    Component.translatable(Sources.LOCAL_FILE.getKey("add"), path), false)
                                    );
                                    return 0;
                                })
                        )
                )
        ).then(
                ClientCommandManager.literal("http").then(
                        ClientCommandManager.argument("path", StringArgumentType.string()).executes(
                                context -> MusicAdderBuilder.execute(context, GETTERS.get(1).get(context), false)
                        )
                )
        );
    }

    public static ArgumentBuilder<CommandSourceStack, ?> insertMusicCommand() {
        return ClientCommandManager.literal("insert").then(
                ClientCommandManager.literal("local").then(
                        ClientCommandManager.argument("path", StringArgumentType.string()).executes(
                                context -> MusicAdderBuilder.execute(context, GETTERS.get(0).get(context), true)
                        )
                )
        ).then(
                ClientCommandManager.literal("http").then(
                        ClientCommandManager.argument("path", StringArgumentType.string()).executes(
                                context -> MusicAdderBuilder.execute(context, GETTERS.get(1).get(context), true)
                        )
                )
        );
    }
}
