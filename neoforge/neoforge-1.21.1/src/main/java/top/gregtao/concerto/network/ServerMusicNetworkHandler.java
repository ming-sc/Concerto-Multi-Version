package top.gregtao.concerto.network;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import top.gregtao.concerto.ConcertoServer;
import top.gregtao.concerto.core.api.MusicJsonParsers;
import top.gregtao.concerto.command.ConcertoServerCommand;
import top.gregtao.concerto.core.config.PresetPlaylistsConfig;
import top.gregtao.concerto.core.config.ServerConfig;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.music.meta.music.MusicMetaData;
import top.gregtao.concerto.network.room.MusicRoom;
import top.gregtao.concerto.network.room.ServerMusicAgent;
import top.gregtao.concerto.util.CommandUtil;
import top.gregtao.concerto.util.RenderUtil;
import top.gregtao.concerto.core.util.TextUtil;

import java.util.*;

public class ServerMusicNetworkHandler {
    public static Map<UUID, MusicDataPacket> WAIT_AUDITION = new HashMap<>();

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ConcertoNetworking.VERSION).optional();
        registrar.playBidirectional(
                ConcertoPayload.TYPE,
                ConcertoPayload.CODEC,
                new DirectionalPayloadHandler<>(
                        (payload, context) -> {},
                        ServerMusicNetworkHandler::generalReceiver
                )
        );
    }

    public static void removeFirst() {
        Iterator<Map.Entry<UUID, MusicDataPacket>> iterator = WAIT_AUDITION.entrySet().iterator();
        if (!iterator.hasNext()) return;
        Map.Entry<UUID, MusicDataPacket> entry = iterator.next();
        sendS2CAuditionSyncData(entry.getKey(), entry.getValue(), true);
        iterator.remove();
    }
    
    public static void generalReceiver(ConcertoPayload payload, IPayloadContext context) {
        switch (payload.channel) {
            case MUSIC_DATA -> musicDataReceiver(payload, context);
            case MUSIC_ROOM -> MusicRoom.serverReceiver(payload, context);
            case MUSIC_AGENT -> musicAgentReceiver(payload, context);
        }
    }

    public static void passAudition(@Nullable Player auditor, UUID uuid) {
        if (WAIT_AUDITION.containsKey(uuid)) {
            MusicDataPacket packet = WAIT_AUDITION.get(uuid);
            WAIT_AUDITION.remove(uuid);
            boolean success = sendS2CMusicData(packet, true);
            if (auditor != null) {
                if (success) {
                    auditor.displayClientMessage(Component.translatable("concerto.audit.pass", packet.from, packet.music.getMeta().title()), false);
                } else {
                    auditor.displayClientMessage(Component.translatable("concerto.share.s2c_failed", uuid.toString()), false);
                }
                ConcertoServer.LOGGER.info("Auditor {} passed request from {}: {} to {}",
                        auditor.getName().getString(), packet.from, packet.music.getMeta().title(), packet.to);
            }
            ConcertoServer.LOGGER.info("Auditor ??? passed request from {}: {} to {}",
                    packet.from, packet.music.getMeta().title(), packet.to);
            sendS2CAuditionSyncData(uuid, packet, true);
        } else if (auditor != null) {
            auditor.displayClientMessage(Component.translatable("concerto.audit.uuid_not_found"), false);
        }
    }

    public static void rejectAll(@Nullable Player auditor) {
        WAIT_AUDITION.forEach((uuid, packet) -> {
            Player player = packet.server.getPlayerList().getPlayerByName(packet.from);
            String title = packet.music.getMeta().title();
            if (player != null) player.displayClientMessage(Component.translatable("concerto.share.rejected", title), false);
        });
        WAIT_AUDITION.clear();
        if (auditor != null) auditor.displayClientMessage(Component.translatable("concerto.audit.reject", "ALL", "ALL"), false);
        ConcertoServer.LOGGER.info("Auditor {} rejected all request", auditor == null ? "?" : auditor.getName().getString());
    }

    public static void rejectAudition(@Nullable Player auditor, UUID uuid) {
        if (WAIT_AUDITION.containsKey(uuid)) {
            MusicDataPacket packet = WAIT_AUDITION.get(uuid);
            WAIT_AUDITION.remove(uuid);
            Player player = packet.server.getPlayerList().getPlayerByName(packet.from);
            String title = packet.music.getMeta().title();
            if (player != null) player.displayClientMessage(Component.translatable("concerto.share.rejected", title), false);
            if (auditor != null) auditor.displayClientMessage(Component.translatable(
                    "concerto.audit.reject", player == null ? "an unknown player" : player.getName().getString(), title), false);
            ConcertoServer.LOGGER.info("Auditor {} rejected request from {}: {} to {}",
                    auditor == null ? "???" : auditor.getName().getString(), packet.from, title, packet.to);
            sendS2CAuditionSyncData(uuid, packet, true);
        } else if (auditor != null) {
            auditor.displayClientMessage(Component.translatable("concerto.audit.uuid_not_found"), false);
        }
    }

    public static void sendAuditionSyncPacket(UUID uuid, ServerPlayer player, MusicDataPacket packet, boolean isDelete) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.AUDITION_SYNC, (isDelete ? "DEL;" : "ADD;") + uuid + ";" +
                (isDelete ? "QwQ" : Objects.requireNonNull(MusicJsonParsers.to(packet.music)).toString()));
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendS2CAuditionSyncData(UUID uuid, MusicDataPacket packet, boolean isDelete) {
        PlayerList playerManager = packet.server.getPlayerList();
        for (ServerPlayer player : playerManager.getPlayers()) {
            if (player.hasPermissions(packet.server.getOperatorUserPermissionLevel())) {
                sendAuditionSyncPacket(uuid, player, packet, isDelete);
            }
        }
    }

    public static void sendS2CAllAuditionData(ServerPlayer player) {
        WAIT_AUDITION.forEach((uuid, packet) -> sendAuditionSyncPacket(uuid, player, packet, false));
    }

    public static void sendS2CPresetRadiosPacket(ServerPlayer player) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.PRESET_RADIOS,
                PresetPlaylistsConfig.PRESET_RADIOS.toString());
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static boolean sendS2CMusicData(MusicDataPacket packet, boolean audit) {
        if (!packet.isS2C) {
            throw new RuntimeException("Not an S2C music data packet");
        } else if (packet.server == null || !packet.server.isRunning()) {
            throw new RuntimeException("Server not found or not running");
        }
        ConcertoPayload payload = packet.toPacket();
        PlayerList playerManager = packet.server.getPlayerList();
        ConcertoServer.LOGGER.info("Trying to send music request to {}", packet.to);
        if (packet.to.equals("@a")) {
            playerManager.getPlayers().forEach(serverPlayer ->
                    PacketDistributor.sendToPlayer(serverPlayer, payload));
        } else {
            ServerPlayer target = playerManager.getPlayerByName(packet.to);
            ServerPlayer from = playerManager.getPlayerByName(packet.from);
            if (target == null) {
                if (from != null) {
                    from.sendSystemMessage(Component.translatable("concerto.share.s2c_player_not_found", packet.to));
                }
                ConcertoServer.LOGGER.warn("Target not found, failed to send.");
                return false;
            } else {
                PacketDistributor.sendToPlayer(target, payload);
                if (audit && from != null) {
                    from.sendSystemMessage(Component.translatable("concerto.share.audition_passed",
                            packet.to, packet.music.getMeta().title()));
                }
            }
        }
        ConcertoServer.LOGGER.info("Successfully.");
        return true;
    }

    public static void musicDataReceiver(ConcertoPayload payload, IPayloadContext context) {
        ServerPlayer player = getServerPlayer(context.player());
        if (player == null) {
            ConcertoServer.LOGGER.warn("Received a music data packet from a local player, ignored.");
            return;
        }
        MinecraftServer server = context.player().getServer();
        try {
            MusicDataPacket packet = MusicDataPacket.fromPacket(payload, false);
            if (packet != null && packet.music != null && server != null) {
                PlayerList playerManager = server.getPlayerList();
                if (!playerExist(playerManager, packet.to)) {
                    player.sendSystemMessage(Component.translatable("concerto.share.c2s_player_not_found", packet.to));
                    ConcertoServer.LOGGER.info("Received a music request from {} to an unknown player", player.getName().getString());
                } else {
                    packet.from = player.getName().getString();
                    packet.isS2C = true;
                    packet.server = server;
                    boolean audit = ServerConfig.INSTANCE.options.auditionRequired && packet.to.equals("@a");
                    boolean success = true;
                    if (audit) {
                        UUID uuid = UUID.randomUUID();
                        for (ServerPlayer player1 : playerManager.getPlayers()) {
                            if (player1.hasPermissions(server.getOperatorUserPermissionLevel())) {
                                player1.sendSystemMessage(CommandUtil.PAGE_SPLIT);
                                player1.sendSystemMessage(ConcertoServerCommand.chatMessageBuilder(
                                        uuid, packet.from, packet.music.getMeta().title()
                                ));
                                player1.sendSystemMessage(CommandUtil.PAGE_SPLIT);
                                sendAuditionSyncPacket(uuid, player1, packet, false);
                            }
                        }
                        WAIT_AUDITION.put(uuid, packet);
                        if (WAIT_AUDITION.size() > ConcertoNetworking.WAIT_LIST_MAX_SIZE) {
                            removeFirst();
                        }
                    } else {
                        success = sendS2CMusicData(packet, false);
                    }
                    player.sendSystemMessage(Component.translatable("concerto.share." + (success ? "success" : "failed")
                            + (audit ? "_audit" : ""), packet.music.getMeta().title()));
                    MusicMetaData meta = packet.music.getMeta();
                    ConcertoServer.LOGGER.info("Received a music request {} - {} from {} to {}",
                            meta.getSource(), meta.title(), player.getName().getString(), packet.to);
                }
            } else {
                player.sendSystemMessage(Component.translatable("concerto.share.error"));
                ConcertoServer.LOGGER.warn("Received an unknown music data packet from {}", player.getName().getString());
            }
        } catch (Exception e) {
            ConcertoServer.LOGGER.warn("Received an unsafe music data packet from {}", player.getName().getString());
            // Ignore unsafe music
        }
    }

    public static void playerJoinHandshake(ServerPlayer player) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.HANDSHAKE,
                ConcertoNetworking.HANDSHAKE_STRING + "CallJoin:" + player.getName().getString()
                        + (ServerConfig.INSTANCE.options.serverMusicAgent && ServerConfig.INSTANCE.options.agentInviteWhenJoin ? ":Invite" : ""));
        PacketDistributor.sendToPlayer(player, payload);
        sendS2CAllAuditionData(player);
        sendS2CPresetRadiosPacket(player);
    }

    public static boolean playerExist(PlayerList manager, String name) {
        return name.equals("@a") || (manager.getPlayerByName(name) != null);
    }

    public static void musicAgentSendMusic(ServerPlayer player, Music music) {
        JsonObject object = MusicJsonParsers.to(music, true);
        if (object == null) return;
        musicAgentSendMusic(player, object.toString());
    }

    public static void musicAgentSendMusic(ServerPlayer player, String music) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.MUSIC_AGENT,
                TextUtil.toBase64(music));
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void musicAgentSendStop(ServerPlayer player) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.MUSIC_AGENT, "Stop");
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void musicAgentSendMusic(List<ServerPlayer> players, Music music) {
        JsonObject object = MusicJsonParsers.to(music, true);
        if (object == null) return;
        players.forEach(player -> musicAgentSendMusic(player, object.toString()));
    }

    public static void musicAgentReceiver(ConcertoPayload payload, IPayloadContext context) {
        ServerPlayer player = getServerPlayer(context.player());
        if (player == null) {
            ConcertoServer.LOGGER.warn("Received a music agent packet from a local player, ignored.");
            return;
        }
        if (!ServerConfig.INSTANCE.options.serverMusicAgent) {
            player.sendSystemMessage(Component.translatable("concerto.agent.not_available"));
            return;
        }
        String[] args = payload.string.split(":");
        if (args[0].equals("Join")) {
            ServerMusicAgent.INSTANCE.playerJoin(player);
            player.sendSystemMessage(Component.translatable("concerto.agent.join"));
        } else if (args[0].equals("Quit")) {
            ServerMusicAgent.INSTANCE.playerQuit(player);
            player.sendSystemMessage(Component.translatable("concerto.agent.quit"));
        } else if (args[0].equals("Query")) {
            List<Music> list = ServerMusicAgent.INSTANCE.getMusicQueue();
            player.sendSystemMessage(CommandUtil.PAGE_SPLIT);
            list.forEach(music -> player.sendSystemMessage(Component.literal(
                    music.getMeta().title() + " - " + music.getMeta().author())));
            player.sendSystemMessage(CommandUtil.PAGE_SPLIT);
        } else if (args.length < 2 || !ServerMusicAgent.INSTANCE.isMember(player)) {
            player.sendSystemMessage(Component.translatable("concerto.agent.error"));
        } else if (args[0].equals("Vote")) {
            if (args[1].equals("New")) {
                ServerMusicAgent.INSTANCE.receiveVoteRequest(player);
            } else if (args[1].length() == 1) {
                ServerMusicAgent.INSTANCE.receiveVote(player, args[1].equals("1"));
            } else {
                player.sendSystemMessage(Component.translatable("concerto.agent.error"));
            }
        } else if (args[0].equals("Add")) {
            Music music = MusicJsonParsers.from(TextUtil.fromBase64(args[1]), false);
            if (music != null && MusicDataPacket.isMusicSafe(music)) {
                ServerMusicAgent.INSTANCE.addMusic(player, music);
            } else {
                player.sendSystemMessage(Component.translatable("concerto.agent.error"));
            }
        }
    }

    public static void sendVote2Member(ServerPlayer player) {
        player.sendSystemMessage(CommandUtil.PAGE_SPLIT);
        player.sendSystemMessage(Component.translatable("concerto.agent.vote")
                .append(Component.literal("  ["))
                .append(Component.translatable("concerto.accept").setStyle(
                        RenderUtil.getRunCommandStyle("/musicroom agent vote true").withColor(ChatFormatting.GREEN)))
                .append(Component.literal("]"))
                .append(Component.literal("  ["))
                .append(Component.translatable("concerto.reject").setStyle(
                        RenderUtil.getRunCommandStyle("/musicroom agent vote false").withColor(ChatFormatting.RED)))
                .append(Component.literal("]")));
        player.sendSystemMessage(CommandUtil.PAGE_SPLIT);
    }

    public static ServerPlayer getServerPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        return null;
    }
}
