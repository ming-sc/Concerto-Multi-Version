package top.gregtao.concerto.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import top.gregtao.concerto.command.argument.ShareMusicTargetArgumentType;
import top.gregtao.concerto.core.api.UnsafeMusicException;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.network.MusicDataPacket;
import top.gregtao.concerto.port.command.ClientCommandManager;
import top.gregtao.concerto.util.CommandUtil;
import top.gregtao.concerto.util.RenderUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ShareMusicCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("sharemusic").then(
                        ClientCommandManager.literal("to").then(
                                ClientCommandManager.argument("target", ShareMusicTargetArgumentType.create()).executes(context -> {
                                    String target = ShareMusicTargetArgumentType.get(context, "target");
                                    ConcertoRunner.run(() -> {
                                        Music current = MusicPlayerHandler.INSTANCE.getCurrentMusic();
                                        if (current != null) {
                                            CommandUtil.commandMessageClient(context, new TranslationTextComponent("concerto.share.sent"));
                                            try {
                                                ClientMusicNetworkHandler.sendC2SMusicData(new MusicDataPacket(current, target, false));
                                            } catch (UnsafeMusicException e) {
                                                CommandUtil.commandMessageClient(context, new TranslationTextComponent("concerto.share.unsafe"));
                                            }
                                        } else {
                                            CommandUtil.commandMessageClient(context, new TranslationTextComponent("concerto.share.no_music"));
                                        }
                                    });
                                    return 0;
                                })
                        )
                ).then(
                        ClientCommandManager.literal("accept").then(
                                ClientCommandManager.argument("uuid", UUIDArgument.uuid()).executes(context -> {
                                    UUID uuid = context.getArgument("uuid", UUID.class);
                                    ClientMusicNetworkHandler.accept(getPlayerFromContext(context), uuid, Minecraft.getInstance());
                                    return 0;
                                })
                        )
                ).then(
                        ClientCommandManager.literal("reject").then(
                                ClientCommandManager.argument("uuid", UUIDArgument.uuid()).executes(context -> {
                                    UUID uuid = context.getArgument("uuid", UUID.class);
                                    ClientMusicNetworkHandler.reject(getPlayerFromContext(context), uuid, Minecraft.getInstance());
                                    return 0;
                                })
                        ).then(ClientCommandManager.literal("all").executes(context -> {
                            ClientMusicNetworkHandler.rejectAll(getPlayerFromContext(context), Minecraft.getInstance());
                            return 0;
                        }))
                ).then(
                        ClientCommandManager.literal("list").then(
                                ClientCommandManager.argument("page", IntegerArgumentType.integer(1)).executes(context -> {
                                    ConcertoRunner.run(() -> {
                                        int page = IntegerArgumentType.getInteger(context, "page");
                                        Map<UUID, MusicDataPacket> map = ClientMusicNetworkHandler.WAIT_CONFIRMATION;
                                        Iterator<Map.Entry<UUID, MusicDataPacket>> iterator = map.entrySet().iterator();
                                        page = Math.min(page, (int) Math.ceil(map.size() / 10f));
                                        CommandUtil.commandMessageClient(context, CommandUtil.PAGE_SPLIT);
                                        for (int i = 1; i < 10 * (page - 1); ++i) {
                                            if (iterator.hasNext()) iterator.next();
                                        }
                                        for (int i = 10 * (page - 1); i < Math.min(10 * page, map.size()) && iterator.hasNext(); ++i) {
                                            Map.Entry<UUID, MusicDataPacket> entry = iterator.next();
                                            MusicDataPacket packet = entry.getValue();
                                            CommandUtil.commandMessageClient(context, new StringTextComponent((i + 1) + ". ").append(chatMessageBuilder(
                                                    entry.getKey(), packet.from, packet.music.getMeta().title()
                                            )));
                                        }
                                        CommandUtil.commandMessageClient(context, CommandUtil.PAGE_SPLIT);
                                    });
                                    return 0;
                                })
                        )
                )
        );
    }

    public static ITextComponent chatMessageBuilder(UUID uuid, String name, String title) {
        return new TranslationTextComponent("concerto.share.wait_confirmation", name, title)
                .append(new StringTextComponent("  ["))
                .append(new TranslationTextComponent("concerto.accept").setStyle(
                        RenderUtil.getRunCommandStyle("/sharemusic accept " + uuid).withColor(TextFormatting.GREEN)))
                .append(new StringTextComponent("]"))
                .append(new StringTextComponent("  ["))
                .append(new TranslationTextComponent("concerto.reject").setStyle(
                        RenderUtil.getRunCommandStyle("/sharemusic reject " + uuid).withColor(TextFormatting.RED)))
                .append(new StringTextComponent("]"));
    }

    public static PlayerEntity getPlayerFromContext(CommandContext<CommandSource> context) {
        Entity entity = context.getSource().getEntity();
        if (entity instanceof PlayerEntity) {
            return (PlayerEntity) entity;
        }
        return null;
    }
}
