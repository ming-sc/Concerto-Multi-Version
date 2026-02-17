package top.gregtao.concerto.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.gregtao.concerto.port.PlayerUtil;

public class CommandUtil {
    public static Component PAGE_SPLIT = Component.literal("==============================================").withStyle(ChatFormatting.DARK_AQUA);

    public static void commandMessageClient(CommandContext<CommandSourceStack> context, Component text) {
        LocalPlayer player = PlayerUtil.getLocalPlayer();
        if (player != null) player.displayClientMessage(text, false);
    }

    public static void commandMessageServer(CommandContext<CommandSourceStack> context, Component text) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) player.sendSystemMessage(text);
    }
}
