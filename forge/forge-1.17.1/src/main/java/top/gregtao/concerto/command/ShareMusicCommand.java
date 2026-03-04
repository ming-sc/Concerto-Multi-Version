package top.gregtao.concerto.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import top.gregtao.concerto.core.api.UnsafeMusicException;
import top.gregtao.concerto.command.argument.ShareMusicTargetArgumentType;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.network.MusicDataPacket;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.port.command.ClientCommandManager;
import top.gregtao.concerto.util.CommandUtil;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.util.RenderUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ShareMusicCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("sharemusic").then(
                        ClientCommandManager.literal("to").then(
                                ClientCommandManager.argument("target", ShareMusicTargetArgumentType.create()).executes(context -> {
                                    String target = ShareMusicTargetArgumentType.get(context, "target");
                                    ConcertoRunner.run(() -> {
                                        Music current = MusicPlayerHandler.INSTANCE.getCurrentMusic();
                                        if (current != null) {
                                            CommandUtil.commandMessageClient(context, new TranslatableComponent("concerto.share.sent"));
                                            try {
                                                ClientMusicNetworkHandler.sendC2SMusicData(new MusicDataPacket(current, target, false));
                                            } catch (UnsafeMusicException e) {
                                                CommandUtil.commandMessageClient(context, new TranslatableComponent("concerto.share.unsafe"));
                                            }
                                        } else {
                                            CommandUtil.commandMessageClient(context, new TranslatableComponent("concerto.share.no_music"));
                                        }
                                    });
                                    return 0;
                                })
                        )
                ).then(
                        ClientCommandManager.literal("accept").then(
                                ClientCommandManager.argument("uuid", UuidArgument.uuid()).executes(context -> {
                                    UUID uuid = context.getArgument("uuid", UUID.class);
                                    ClientMusicNetworkHandler.accept(getPlayerFromContext(context), uuid, Minecraft.getInstance());
                                    return 0;
                                })
                        )
                ).then(
                        ClientCommandManager.literal("reject").then(
                                ClientCommandManager.argument("uuid", UuidArgument.uuid()).executes(context -> {
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
                                            CommandUtil.commandMessageClient(context, new TextComponent((i + 1) + ". ").append(chatMessageBuilder(
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

    public static Component chatMessageBuilder(UUID uuid, String name, String title) {
        return new TranslatableComponent("concerto.share.wait_confirmation", name, title)
                .append(new TextComponent("  ["))
                .append(new TranslatableComponent("concerto.accept").setStyle(
                        RenderUtil.getRunCommandStyle("/sharemusic accept " + uuid).withColor(ChatFormatting.GREEN)))
                .append(new TextComponent("]"))
                .append(new TextComponent("  ["))
                .append(new TranslatableComponent("concerto.reject").setStyle(
                        RenderUtil.getRunCommandStyle("/sharemusic reject " + uuid).withColor(ChatFormatting.RED)))
                .append(new TextComponent("]"));
    }

    public static Player getPlayerFromContext(CommandContext<CommandSourceStack> context) {
        Entity entity = context.getSource().getEntity();
        if (entity instanceof Player) {
            return (Player) entity;
        }
        return null;
    }
}
