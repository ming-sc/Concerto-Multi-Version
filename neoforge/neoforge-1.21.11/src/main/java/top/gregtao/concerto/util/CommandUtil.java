package top.gregtao.concerto.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.gregtao.concerto.port.PlayerUtil;

public class CommandUtil {
    public static Component PAGE_SPLIT = Component.literal("==============================================").withStyle(ChatFormatting.DARK_AQUA);

    public static void commandMessageClient(CommandContext<CommandSourceStack> context, Component text) {
        LocalPlayer player = PlayerUtil.getLocalPlayer();
        displayClientMessage(player, text, false);
    }

    public static void commandMessageServer(CommandContext<CommandSourceStack> context, Component text) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) player.sendSystemMessage(text);
    }

    public static void displayClientMessage(LocalPlayer player, Component message, boolean actionBar) {
        if (player != null) {
            Minecraft.getInstance().submit(() -> {
                player.displayClientMessage(message, actionBar);
            });
        }
    }

    public static void displayClientMessage(Player player, Component message, boolean actionBar) {
        if (player.isLocalPlayer()) {
            displayClientMessage((LocalPlayer) player, message, actionBar);
        } else {
            player.displayClientMessage(message, actionBar);
        }
    }
}
