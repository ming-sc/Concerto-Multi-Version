package top.gregtao.concerto.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import top.gregtao.concerto.port.PlayerUtil;

public class CommandUtil {
    public static ITextComponent PAGE_SPLIT = new StringTextComponent("==============================================").withStyle(TextFormatting.DARK_AQUA);

    public static void commandMessageClient(CommandContext<CommandSource> context, ITextComponent text) {
        ClientPlayerEntity player = PlayerUtil.getLocalPlayer();
        if (player != null) player.displayClientMessage(text, false);
    }

    public static void commandMessageServer(CommandContext<CommandSource> context, ITextComponent text) {
        ServerPlayerEntity player;
        try {
            player = context.getSource().getPlayerOrException();
            if (player != null) player.displayClientMessage(text, false);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
