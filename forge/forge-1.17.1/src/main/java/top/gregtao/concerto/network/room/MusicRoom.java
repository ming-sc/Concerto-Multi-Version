package top.gregtao.concerto.network.room;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.ConcertoServer;
import top.gregtao.concerto.core.api.DynamicPath;
import top.gregtao.concerto.core.api.MusicJsonParsers;
import top.gregtao.concerto.core.config.ServerConfig;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.music.SharedMusic;
import top.gregtao.concerto.network.ConcertoPayload;
import top.gregtao.concerto.network.MusicDataPacket;
import top.gregtao.concerto.network.NetworkHelper;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.util.TextUtil;

import java.util.*;

public class MusicRoom {

    //TODO: 或许需要重写，但是没空了

    public static final Map<UUID, MusicRoom> ROOMS = new HashMap<>(); // Server side

    public final UUID uuid;
    public String owner;
    public Music music;
    public boolean pause = true;
    public Map<String, Integer> members = new HashMap<>();

    public int permission = 0; // Client Side

    public MusicRoom(String creator) {
        this.uuid = UUID.randomUUID();
        this.owner = creator;
        this.members.put(creator, 3); // 0: banned, 1: common, 2: admin, 3:super_admin
    }

    public MusicRoom(UUID uuid) {
        this.uuid = uuid;
    }

    public String buildArgs(boolean withMusic) {
        return this.uuid + ":" + this.owner + ":" + (this.pause ? "1" : "0") + ":"
                + String.join(",", this.members.entrySet().stream().map(entry -> entry.getKey() + "+" + entry.getValue()).toList())
                + ":" + (!withMusic || this.music == null ? "null" : TextUtil.toBase64(MusicJsonParsers.to(music).toString()));
    }

    public void send2EachMember(String command, String args, String ignores, MinecraftServer server) {
        List<String> strings = Arrays.stream(ignores.split(",")).toList();
        this.members.forEach((member, permission) -> {
            if (!strings.contains(member)) {
                serverSender(command, args, server.getPlayerList().getPlayerByName(member));
            }
        });
    }

    public void serverOnRemove(String name, MinecraftServer server) throws IllegalAccessException {
        if (this.members.get(name) < 2) throw new IllegalAccessException("No permission");
        this.send2EachMember("REM", "", "", server);
    }

    public void serverOnJoin(String name, MinecraftServer server) {
        this.members.put(name, 1);
        this.send2EachMember("UPD", this.buildArgs(false), "", server);
    }

    public void serverOnQuit(String name, MinecraftServer server) {
        this.members.remove(name);
        this.send2EachMember("UPD", this.buildArgs(false), "", server);
    }

    public void serverOnUpdate(String name, String music, MinecraftServer server) throws IllegalAccessException {
        if (this.members.get(name) < 2) throw new IllegalAccessException("No permission");
        this.music = MusicJsonParsers.from(TextUtil.fromBase64(music));
        this.pause = false;
        this.send2EachMember("UPD", this.buildArgs(true), name, server);
    }

    public void serverOnPause(String name, boolean pause, MinecraftServer server) throws IllegalAccessException {
        if (this.members.get(name) < 2) throw new IllegalAccessException("No permission");
        this.pause = pause;
        this.send2EachMember("UPD", this.buildArgs(false), name, server);
    }

    public void serverOnSetOp(String name, ServerPlayer player, String target, MinecraftServer server) throws IllegalAccessException {
        if (this.members.get(name) < 3 || name.equals(target)) throw new IllegalAccessException("No permission");
        Integer permission = this.members.get(target);
        if (permission == null) {
            player.displayClientMessage(new TranslatableComponent("concerto.room.update.fail"), false);
        } else if (permission == 2) {
            this.members.put(target, 1);
            player.displayClientMessage(new TranslatableComponent("concerto.room.de_op", target), false);
        } else {
            this.members.put(target, 2);
            player.displayClientMessage(new TranslatableComponent("concerto.room.op", target), false);
        }
        this.send2EachMember("UPD", this.buildArgs(false), name, server);
    }

    public static void serverSender(String command, String args, ServerPlayer player) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.MUSIC_ROOM, command + ":" + args);
        NetworkHelper.sendToPlayer(player, payload);
    }

    public static void serverReceiver(ConcertoPayload payload, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        MinecraftServer server = context.getSender().getServer();
        String[] args = payload.string.split(":");
        switch (args[0]) {
            case "CRE": {
                if (!player.hasPermissions(ServerConfig.INSTANCE.options.musicRoomCommandPermission)) {
                    player.displayClientMessage(new TranslatableComponent("concerto.room.permission_denied"), false);
                    break;
                }
                MusicRoom room = new MusicRoom(player.getName().getString());
                ROOMS.put(room.uuid, room);
                serverSender("JOI", room.buildArgs(false), player);
                player.displayClientMessage(new TranslatableComponent("concerto.room.create", room.uuid.toString()), false);
                break;
            }
            case "REM": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    MusicRoom room = Objects.requireNonNull(ROOMS.get(uuid1));
                    room.serverOnRemove(player.getName().getString(), server);
                    ROOMS.remove(uuid1);
                    player.displayClientMessage(new TranslatableComponent("concerto.room.remove", uuid1.toString()), false);
                } catch (NullPointerException | IllegalArgumentException | IllegalAccessException e) {
                    ConcertoServer.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.remove.fail"), false);
                }
                break;
            }
            case "JOI": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    MusicRoom room = Objects.requireNonNull(ROOMS.get(uuid1));
                    room.serverOnJoin(player.getName().getString(), server);
                    serverSender("JOI", room.buildArgs(true), player);
                    player.displayClientMessage(new TranslatableComponent("concerto.room.join", uuid1.toString()), false);
                } catch (NullPointerException | IllegalArgumentException e) {
                    ConcertoServer.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.join.fail"), false);
                }
                break;
            }
            case "QUI": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    MusicRoom room = Objects.requireNonNull(ROOMS.get(uuid1));
                    room.serverOnQuit(player.getName().getString(), server);
                    serverSender("QUI", uuid1.toString(), player);
                    player.displayClientMessage(new TranslatableComponent("concerto.room.quit", uuid1.toString()), false);
                } catch (NullPointerException | IllegalArgumentException e) {
                    ConcertoServer.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.quit.fail"), false);
                }
                break;
            }
            case "UPD": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    MusicRoom room = Objects.requireNonNull(ROOMS.get(uuid1));
                    room.serverOnUpdate(player.getName().getString(), args[2], server);
                } catch (NullPointerException | IllegalArgumentException | IllegalAccessException e) {
                    ConcertoServer.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.update.fail"), false);
                }
                break;
            }
            case "PAU": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    MusicRoom room = Objects.requireNonNull(ROOMS.get(uuid1));
                    room.serverOnPause(player.getName().getString(), args[2].equals("1"), server);
                } catch (NullPointerException | IllegalArgumentException | IllegalAccessException e) {
                    ConcertoServer.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.update.fail"), false);
                }
                break;
            }
            case "SOP": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    MusicRoom room = Objects.requireNonNull(ROOMS.get(uuid1));
                    room.serverOnSetOp(player.getName().getString(), player, args[2], server);
                } catch (NullPointerException | IllegalArgumentException | IllegalAccessException e) {
                    ConcertoServer.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.update.fail"), false);
                }
                break;
            }
        }
    }

    public static MusicRoom CLIENT_ROOM;

    public static void clientCreate() {
        if (CLIENT_ROOM != null) return;
        clientSender("CRE", "");
    }

    public static void clientRemove() {
        if (CLIENT_ROOM == null) return;
        clientSender("REM", CLIENT_ROOM.uuid.toString());
    }

    public static void clientJoin(String uuid) {
        if (CLIENT_ROOM != null) return;
        clientSender("JOI", uuid);
    }

    public static void clientQuit() {
        if (CLIENT_ROOM == null) return;
        if (CLIENT_ROOM.permission == 3) clientRemove();
        else clientSender("QUI", CLIENT_ROOM.uuid.toString());
    }

    public static void clientUpdate(Music music) {
        if (CLIENT_ROOM == null || CLIENT_ROOM.permission < 2) return;
        if (!MusicDataPacket.isMusicSafe(music)) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("concerto.share.unsafe"), false);
            }
            return;
        }
        Music music1;
        if (music instanceof DynamicPath dynamicPath) {
            String path = dynamicPath.getLastRawPath();
            music1 = new SharedMusic(path, music.getMeta(), dynamicPath.getLastLyrics(), dynamicPath.getLastSubLyrics());
        } else {
            music1 = music;
        }
        clientSender("UPD", CLIENT_ROOM.uuid.toString() + ":" + TextUtil.toBase64(MusicJsonParsers.to(music1).toString()));
    }

    public static void clientPause(boolean pause) {
        if (CLIENT_ROOM == null || CLIENT_ROOM.permission < 2) return;
        clientSender("PAU", CLIENT_ROOM.uuid.toString() + (pause ? ":1" : ":0"));
    }

    public static void clientSetOp(String target) {
        if (CLIENT_ROOM == null || CLIENT_ROOM.permission < 3) return;
        clientSender("SOP", CLIENT_ROOM.uuid.toString() + ":" + target);
        CLIENT_ROOM.members.put(target, 2);
    }

    public static void clientSender(String command, String args) {
        ConcertoPayload payload = new ConcertoPayload(ConcertoPayload.Channel.MUSIC_ROOM, command + ":" + args);
        NetworkHelper.sendToServer(payload);
    }

    public static void clientReceiver(ConcertoPayload payload, NetworkEvent.Context context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        LocalPlayer player = client.player;
        String[] args = payload.string.split(":");
        switch (args[0]) {
            case "REM": {
                CLIENT_ROOM = null;
                ConcertoClient.clientState = ConcertoClient.ClientState.LOCAL;
                break;
            }
            case "JOI": {
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    CLIENT_ROOM = new MusicRoom(uuid1);
                    client.keyboardHandler.setClipboard(uuid1.toString());
                    CLIENT_ROOM.owner = args[2];
                    CLIENT_ROOM.members = new HashMap<>();
                    Arrays.stream(args[4].split(",")).forEach(str -> {
                        String[] strings = str.split("\\+");
                        int permission = Integer.parseInt(strings[1]);
                        CLIENT_ROOM.members.put(strings[0], permission);
                        if (strings[0].equals(player.getName().getString())) {
                            CLIENT_ROOM.permission = permission;
                        }
                    });
                    CLIENT_ROOM.pause = args[3].equals("1");
                    if (!args[5].equals("null")) {
                        CLIENT_ROOM.music = MusicJsonParsers.from(TextUtil.fromBase64(args[5]));
                        MusicPlayer.INSTANCE.playTempMusic(CLIENT_ROOM.music, () -> {
                            if (CLIENT_ROOM.pause) MusicPlayer.INSTANCE.pause();
                        });
                    }
                    ConcertoClient.clientState = ConcertoClient.ClientState.MUSIC_ROOM;
                    player.displayClientMessage(new TranslatableComponent("concerto.room.join", uuid1.toString()), false);
                } catch (NullPointerException | IllegalArgumentException e) {
                    ConcertoClient.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.join.fail"), false);
                }
                break;
            }
            case "QUI": {
                if (CLIENT_ROOM == null) break;
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    if (CLIENT_ROOM.uuid.compareTo(uuid1) == 0) CLIENT_ROOM = null;
                    ConcertoClient.clientState = ConcertoClient.ClientState.LOCAL;
                    player.displayClientMessage(new TranslatableComponent("concerto.room.quit", uuid1.toString()), false);
                } catch (NullPointerException | IllegalArgumentException e) {
                    ConcertoClient.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.join.fail"), false);
                }
                break;
            }
            case "UPD": {
                if (CLIENT_ROOM == null) break;
                try {
                    UUID uuid1 = UUID.fromString(args[1]);
                    if (CLIENT_ROOM.uuid.compareTo(uuid1) != 0) break;
                    CLIENT_ROOM.owner = args[2];
                    CLIENT_ROOM.members = new HashMap<>();
                    Arrays.stream(args[4].split(",")).forEach(str -> {
                        String[] strings = str.split("\\+");
                        int permission = Integer.parseInt(strings[1]);
                        CLIENT_ROOM.members.put(strings[0], permission);
                        if (strings[0].equals(player.getName().getString())) {
                            CLIENT_ROOM.permission = permission;
                        }
                    });
                    CLIENT_ROOM.pause = args[3].equals("1");
                    if (!args[5].equals("null")) {
                        CLIENT_ROOM.music = MusicJsonParsers.from(TextUtil.fromBase64(args[5]));
                        MusicPlayer.INSTANCE.playTempMusic(CLIENT_ROOM.music, () -> {
                            if (CLIENT_ROOM.pause) MusicPlayer.INSTANCE.pause();
                        });
                    } else if (CLIENT_ROOM.pause) {
                        MusicPlayer.INSTANCE.musicRoomPause();
                    } else {
                        MusicPlayer.INSTANCE.musicRoomResume();
                    }
                } catch (NullPointerException | IllegalArgumentException e) {
                    ConcertoClient.LOGGER.warn(e.toString());
                    player.displayClientMessage(new TranslatableComponent("concerto.room.update.fail"), false);
                }
                break;
            }
        }
    }
}
