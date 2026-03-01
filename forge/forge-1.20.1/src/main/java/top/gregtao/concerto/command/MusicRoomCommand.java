package top.gregtao.concerto.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.network.room.MusicRoom;
import top.gregtao.concerto.port.PlayerUtil;
import top.gregtao.concerto.port.command.ClientCommandManager;

public class MusicRoomCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("musicroom")
                        .then(ClientCommandManager.literal("create").executes(context -> {
                            LocalPlayer player = PlayerUtil.getLocalPlayer();
                            if (checkServerAvailable(player) && checkLocal(player)) {
                                MusicRoom.clientCreate();
                            }
                            return 0;
                        })).then(ClientCommandManager.literal("join").then(
                                ClientCommandManager.argument("uuid", StringArgumentType.string()).executes(context -> {
                                    LocalPlayer player = PlayerUtil.getLocalPlayer();
                                    if (checkServerAvailable(player) && checkLocal(player)) {
                                        MusicRoom.clientJoin(StringArgumentType.getString(context, "uuid"));
                                    }
                                    return 0;
                                })
                        )).then(ClientCommandManager.literal("quit").executes(context -> {
                            switch (ConcertoClient.clientState) {
                                case MUSIC_AGENT -> ClientMusicNetworkHandler.musicAgentQuit();
                                case MUSIC_ROOM -> MusicRoom.clientQuit();
                            }
                            return 0;
                        })).then(ClientCommandManager.literal("members").executes(context -> {
                            if (MusicRoom.CLIENT_ROOM != null) {
                                PlayerUtil.getLocalPlayer().displayClientMessage(Component.translatable(
                                        "concerto.room.members", MusicRoom.CLIENT_ROOM.owner,
                                        String.join(",", MusicRoom.CLIENT_ROOM.members.keySet())
                                ), false);
                            }
                            return 0;
                        })).then(ClientCommandManager.literal("op").then(
                                ClientCommandManager.argument("player", StringArgumentType.string()).executes(context -> {
                                    MusicRoom.clientSetOp(StringArgumentType.getString(context, "player"));
                                    return 0;
                                })
                        )).then(
                                ClientCommandManager.literal("agent").then(
                                        ClientCommandManager.literal("join").executes(context -> {
                                            LocalPlayer player = PlayerUtil.getLocalPlayer();
                                            if (checkServerAvailable(player) && checkLocal(player)) {
                                                ClientMusicNetworkHandler.musicAgentJoin();
                                            }
                                            return 0;
                                        })
                                ).then(
                                        ClientCommandManager.literal("quit").executes(context -> {
                                            LocalPlayer player = PlayerUtil.getLocalPlayer();
                                            if (checkServerAvailable(player) && checkAgent(player)) {
                                                ClientMusicNetworkHandler.musicAgentQuit();
                                            }
                                            return 0;
                                        })
                                ).then(
                                        ClientCommandManager.literal("query").executes(context -> {
                                            LocalPlayer player = PlayerUtil.getLocalPlayer();
                                            if (checkServerAvailable(player) && checkAgent(player)) {
                                                ClientMusicNetworkHandler.musicAgentQuery();
                                            }
                                            return 0;
                                        })
                                ).then(
                                        ClientCommandManager.literal("add").executes(context -> {
                                            LocalPlayer player = PlayerUtil.getLocalPlayer();
                                            if (checkServerAvailable(player) && checkAgent(player)) {
                                                if (!ClientMusicNetworkHandler.musicAgentAddCurrentMusic()) {
                                                    player.displayClientMessage(Component.translatable("concerto.agent.not_playing"), false);
                                                }
                                            }
                                            return 0;
                                        })
                                ).then(
                                        ClientCommandManager.literal("vote").executes(context -> {
                                            LocalPlayer player = PlayerUtil.getLocalPlayer();
                                            if (checkServerAvailable(player) && checkAgent(player)) {
                                                ClientMusicNetworkHandler.musicAgentNewVote();
                                            }
                                            return 0;
                                        }).then(
                                                ClientCommandManager.argument("vote", BoolArgumentType.bool()).executes(context -> {
                                                    LocalPlayer player = PlayerUtil.getLocalPlayer();
                                                    if (checkServerAvailable(player) && checkAgent(player)) {
                                                        ClientMusicNetworkHandler.musicAgentVote(BoolArgumentType.getBool(context, "vote"));
                                                    }
                                                    return 0;
                                                })
                                        )
                                )
                        )
        );
    }

    public static boolean checkServerAvailable(LocalPlayer player) {
        if (!ConcertoClient.isServerAvailable()) {
            player.displayClientMessage(Component.translatable("concerto.not_available"), false);
            return false;
        }
        return true;
    }

    public static boolean checkLocal(LocalPlayer player) {
        if (ConcertoClient.clientState == ConcertoClient.ClientState.LOCAL) {
            return true;
        } else {
            player.displayClientMessage(Component.translatable("concerto.agent.occupied"), false);
            return false;
        }
    }

    public static boolean checkAgent(LocalPlayer player) {
        if (ConcertoClient.clientState == ConcertoClient.ClientState.MUSIC_AGENT) {
            return true;
        } else {
            player.displayClientMessage(Component.translatable("concerto.agent.not_in"), false);
            return false;
        }
    }
}
