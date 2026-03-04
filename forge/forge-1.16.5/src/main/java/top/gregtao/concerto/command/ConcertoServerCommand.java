package top.gregtao.concerto.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.ConcertoServer;
import top.gregtao.concerto.core.config.CacheManager;
import top.gregtao.concerto.core.http.kugou.KuGouMusicApiClient;
import top.gregtao.concerto.core.http.netease.NeteaseCloudApiClient;
import top.gregtao.concerto.core.http.qq.QQMusicApiClient;
import top.gregtao.concerto.network.MusicDataPacket;
import top.gregtao.concerto.network.ServerMusicNetworkHandler;
import top.gregtao.concerto.network.room.ServerMusicAgent;
import top.gregtao.concerto.util.CommandUtil;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.util.RenderUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ConcertoServerCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("concerto-server").then(
                        Commands.literal("audit").requires(source -> source.hasPermission(2)).then(
                                Commands.argument("uuid", UUIDArgument.uuid()).executes(context -> {
                                    UUID uuid = UUIDArgument.getUuid(context, "uuid");
                                    ServerMusicNetworkHandler.passAudition(context.getSource().getPlayerOrException(), uuid);
                                    return 0;
                                })
                        ).then(
                                Commands.literal("reject").then(
                                        Commands.argument("uuid", UUIDArgument.uuid()).executes(context -> {
                                            UUID uuid = UUIDArgument.getUuid(context, "uuid");
                                            ServerMusicNetworkHandler.rejectAudition(context.getSource().getPlayerOrException(), uuid);
                                            return 0;
                                        })
                                ).then(Commands.literal("all").executes(context -> {
                                    ServerMusicNetworkHandler.rejectAll(context.getSource().getPlayerOrException());
                                    return 0;
                                }))
                        ).then(
                                Commands.literal("list").then(
                                        Commands.argument("page", IntegerArgumentType.integer(1)).executes(context -> {
                                            ConcertoRunner.run(() -> {
                                                int page = IntegerArgumentType.getInteger(context, "page");
                                                Map<UUID, MusicDataPacket> map = ServerMusicNetworkHandler.WAIT_AUDITION;
                                                Iterator<Map.Entry<UUID, MusicDataPacket>> iterator = map.entrySet().iterator();
                                                page = Math.min(page, (int) Math.ceil(map.size() / 10f));
                                                CommandUtil.commandMessageServer(context, CommandUtil.PAGE_SPLIT);
                                                for (int i = 1; i < 10 * (page - 1); ++i) {
                                                    if (iterator.hasNext()) iterator.next();
                                                }
                                                for (int i = 10 * (page - 1); i < Math.min(10 * page, map.size()) && iterator.hasNext(); ++i) {
                                                    Map.Entry<UUID, MusicDataPacket> entry = iterator.next();
                                                    MusicDataPacket packet = entry.getValue();
                                                    CommandUtil.commandMessageServer(context, new StringTextComponent((i + 1) + ". ").append(chatMessageBuilder(
                                                            entry.getKey(), packet.from, packet.music.getMeta().title()
                                                    )));
                                                }
                                                CommandUtil.commandMessageServer(context, CommandUtil.PAGE_SPLIT);
                                            });
                                            return 0;
                                        })
                                )
                        )
                ).then(
                        Commands.literal("reload").requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ConcertoServer.reload();
                                    return 0;
                                })
                ).then(
                        Commands.literal("reload-cookie").requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    NeteaseCloudApiClient.INSTANCE.readCookie();
                                    QQMusicApiClient.INSTANCE.readCookie();
                                    KuGouMusicApiClient.INSTANCE.readCookie();
                                    return 0;
                                })
                ).then(
                        Commands.literal("clean-cache").requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    CacheManager.cleanAllCache();
                                    return 0;
                                })
                ).then(
                        Commands.literal("fetch-radios")
                                .requires(source -> source.hasPermission(0)).executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrException();
                                    if (player != null) ServerMusicNetworkHandler.sendS2CPresetRadiosPacket(player);
                                    return 0;
                                })
                ).then(
                        Commands.literal("agent").requires(source -> source.hasPermission(2)).then(
                                Commands.literal("reset").executes(context -> {
                                    ServerMusicAgent.INSTANCE.reset();
                                    return 0;
                                })
                        ).then(
                                Commands.literal("cut").executes(context -> {
                                    ServerMusicAgent.INSTANCE.schedulePlayNext(0, false);
                                    return 0;
                                })
                        ).then(
                                Commands.literal("stop").executes(context -> {
                                    ServerMusicAgent.INSTANCE.stop();
                                    return 0;
                                })
                        ).then(
                                Commands.literal("start").executes(context -> {
                                    ServerMusicAgent.INSTANCE.start();
                                    return 0;
                                })
                        )
                )
        );
    }

    public static ITextComponent chatMessageBuilder(UUID uuid, String name, String title) {
        return new TranslationTextComponent("concerto.audit.message", name, title)
                .append(new StringTextComponent("  ["))
                .append(new TranslationTextComponent("concerto.accept").setStyle(
                        RenderUtil.getRunCommandStyle("/concerto-server audit " + uuid).withColor(TextFormatting.GREEN)))
                .append(new StringTextComponent("]"))
                .append(new StringTextComponent("  ["))
                .append(new TranslationTextComponent("concerto.reject").setStyle(
                        RenderUtil.getRunCommandStyle("/concerto-server audit reject " + uuid).withColor(TextFormatting.RED)))
                .append(new StringTextComponent("]"));
    }
}
