/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package top.gregtao.concerto.port.command.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.gregtao.concerto.mixin.LocalPlayerAccessor;

import java.util.IdentityHashMap;
import java.util.Map;

public class ClientCommandHandler
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static CommandDispatcher<CommandSource> commands = null;

    /*
     * For internal use
     *
     * Merges command dispatcher use for suggestions to the command dispatcher used for client commands so they can be sent to the server, and vice versa so client commands appear
     * with server commands in suggestions
     */
    public static CommandDispatcher<ISuggestionProvider> mergeServerCommands(CommandDispatcher<ISuggestionProvider> serverCommands)
    {
        CommandDispatcher<CommandSource> commandsTemp = new CommandDispatcher<>();
        MinecraftForge.EVENT_BUS.post(new RegisterClientCommandsEvent(commandsTemp));

        // Copies the client commands into another RootCommandNode so that redirects can't be used with server commands
        commands = new CommandDispatcher<>();
        copy(commandsTemp.getRoot(), commands.getRoot());

        // Copies the server commands into another RootCommandNode so that redirects can't be used with client commands
        RootCommandNode<ISuggestionProvider> serverCommandsRoot = serverCommands.getRoot();
        CommandDispatcher<ISuggestionProvider> newServerCommands = new CommandDispatcher<>();
        copy(serverCommandsRoot, newServerCommands.getRoot());

        // Copies the client side commands into the server side commands to be used for suggestions
        CommandHelper.mergeCommandNode(commands.getRoot(), newServerCommands.getRoot(), new IdentityHashMap<>(), getSource(), (context) -> 0, (suggestions) -> {
            SuggestionProvider<ISuggestionProvider> suggestionProvider = SuggestionProviders
                    .safelySwap((SuggestionProvider<ISuggestionProvider>) (SuggestionProvider<?>) suggestions);
            if (suggestionProvider == SuggestionProviders.ASK_SERVER)
            {
                suggestionProvider = (context, builder) -> {
                    ClientCommandSourceStack source = getSource();
                    StringReader reader = new StringReader(context.getInput());
                    if (reader.canRead() && reader.peek() == '/')
                    {
                        reader.skip();
                    }

                    ParseResults<CommandSource> parse = commands.parse(reader, source);
                    return commands.getCompletionSuggestions(parse);
                };
            }
            return suggestionProvider;
        });

        return newServerCommands;
    }

    /**
     * @return The command dispatcher for client side commands
     */
    public static CommandDispatcher<CommandSource> getDispatcher()
    {
        return commands;
    }

    /**
     * @return A {@link CommandSource} for the player in the current client
     */
    public static ClientCommandSourceStack getSource()
    {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        return new ClientCommandSourceStack(player, player.position(), player.getRotationVector(), ((LocalPlayerAccessor) player).concerto$getPermissionLevel(),
                player.getName().getString(), player.getDisplayName(), player);
    }

    /**
     *
     * Creates a deep copy of the sourceNode while keeping the redirects referring to the old command tree
     *
     * @param sourceNode
     *            the original
     * @param resultNode
     *            the result
     */
    private static <S> void copy(CommandNode<S> sourceNode, CommandNode<S> resultNode)
    {
        Map<CommandNode<S>, CommandNode<S>> newNodes = new IdentityHashMap<>();
        newNodes.put(sourceNode, resultNode);
        for (CommandNode<S> child : sourceNode.getChildren())
        {
            CommandNode<S> copy = newNodes.computeIfAbsent(child, innerChild ->
            {
                ArgumentBuilder<S, ?> builder = innerChild.createBuilder();
                CommandNode<S> innerCopy = builder.build();
                copy(innerChild, innerCopy);
                return innerCopy;
            });
            resultNode.addChild(copy);
        }
    }

    public static boolean sendMessage(String sendMessage)
    {
        StringReader reader = new StringReader(sendMessage);

        if (!reader.canRead() || reader.read() != '/')
        {
            return false;
        }

        ClientCommandSourceStack source = getSource();

        try
        {
            commands.execute(reader, source);
        }
        catch (CommandException execution)// Probably thrown by the command
        {
            Minecraft.getInstance().player.sendMessage(new StringTextComponent("").append(execution.getComponent()).withStyle(TextFormatting.RED), Util.NIL_UUID);
        }
        catch (CommandSyntaxException syntax)// Usually thrown by the CommandDispatcher
        {
            if (syntax.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand() || syntax.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument())
            {
                // in case of unknown command, let the server try and handle it
                return false;
            }
            Minecraft.getInstance().player.sendMessage(
                    new StringTextComponent("").append(TextComponentUtils.fromMessage(syntax.getRawMessage())).withStyle(TextFormatting.RED), Util.NIL_UUID);
            if (syntax.getInput() != null && syntax.getCursor() >= 0)
            {
                int position = Math.min(syntax.getInput().length(), syntax.getCursor());
                IFormattableTextComponent details = new StringTextComponent("")
                        .withStyle(TextFormatting.GRAY)
                        .withStyle((style) -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, reader.getString())));
                if (position > 10)
                {
                    details.append("...");
                }
                details.append(syntax.getInput().substring(Math.max(0, position - 10), position));
                if (position < syntax.getInput().length())
                {
                    details.append(new StringTextComponent(syntax.getInput().substring(position)).withStyle(TextFormatting.RED, TextFormatting.UNDERLINE));
                }
                details.append(new TranslationTextComponent("command.context.here").withStyle(TextFormatting.RED, TextFormatting.ITALIC));
                Minecraft.getInstance().player.sendMessage(new StringTextComponent("").append(details).withStyle(TextFormatting.RED), Util.NIL_UUID);
            }
        }
        catch (Exception generic)// Probably thrown by the command
        {
            StringTextComponent message = new StringTextComponent(generic.getMessage() == null ? generic.getClass().getName() : generic.getMessage());
            Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("command.failed")
                            .withStyle(TextFormatting.RED)
                            .withStyle((style) -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, message))),
                    Util.NIL_UUID);
            LOGGER.error("Error executing client command \"{}\"", sendMessage, generic);
        }
        return true;
    }
}